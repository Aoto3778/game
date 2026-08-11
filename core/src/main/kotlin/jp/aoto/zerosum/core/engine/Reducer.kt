package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.GameEvent
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.core.model.Side
import jp.aoto.zerosum.core.rng.SplitMix64

/** The only state transition entrypoint used by UI, tests, replay, and bots. */
public fun reduce(state: GameState, action: Action): GameState {
    val clean = state.copy(events = emptyList())
    return when (action) {
        is Action.StartRun -> startRun(clean, action)
        is Action.BeginCombat -> beginCombat(clean, action)
        is Action.SelectMapNode -> RunMap.select(clean, action.nodeId)
        is Action.PlayCard -> playCard(clean, action)
        Action.EndTurn -> endTurn(clean)
        is Action.ChooseDraft -> chooseDraft(clean, action)
        is Action.BeginEvent -> EventResolver.begin(clean, action.eventId)
        is Action.ChooseEvent -> EventResolver.choose(clean, action.choiceId)
        Action.Rest -> rest(clean)
        Action.CompleteAct -> completeAct(clean)
        is Action.Navigate -> navigate(clean, action.screen)
        Action.AbandonRun -> abandon(clean)
    }
}

/** Replays an action sequence without hidden state. */
public fun replay(initial: GameState, actions: Iterable<Action>): GameState =
    actions.fold(initial, ::reduce)

private fun startRun(state: GameState, action: Action.StartRun): GameState {
    if (action.ascension !in 0..20) return state.invalid("ascension")
    return StateFactory.newRun(action.heroClass, action.seed, action.ascension)
}

private fun beginCombat(state: GameState, action: Action.BeginCombat): GameState {
    if (state.runStatus != RunStatus.ACTIVE || state.enemy != null) return state.invalid(action.enemyId)
    if (GameCatalog.allEnemies().none { it.id == action.enemyId }) return state.invalid(action.enemyId)
    return StateFactory.beginCombat(state, action.enemyId)
}

private fun playCard(state: GameState, action: Action.PlayCard): GameState {
    if (state.screen != Screen.COMBAT || state.enemy == null) return state.invalid(action.instanceId.toString())
    val card = state.hand.firstOrNull { it.instanceId == action.instanceId }
        ?: return state.invalid(action.instanceId.toString())
    if (card.faceDown) return state.invalid(card.definitionId)
    val definition = GameCatalog.card(card.definitionId)
    val cost = if (card.upgraded) definition.upgradedCost else definition.cost
    if (state.energy < cost) return state.invalid(card.definitionId)
    val removed = state.copy(
        energy = state.energy - cost,
        hand = state.hand.filterNot { it.instanceId == card.instanceId },
    )
    val resolved = EffectResolver.resolveCard(removed, card, Side.PLAYER)
    val tags = if (card.upgraded) definition.upgradedTags else definition.tags
    val moved = if (CardTag.EXHAUST in tags) {
        resolved.copy(exhaustPile = resolved.exhaustPile + card)
    } else {
        resolved.copy(discardPile = resolved.discardPile + card)
    }
    val recorded = moved.copy(
        lastPlayedDefinitionId = card.definitionId,
        stats = moved.stats.copy(cardsPlayed = moved.stats.cardsPlayed + 1),
    )
    return if ((recorded.enemy?.actor?.hp ?: 1) <= 0) finishCombat(recorded) else recorded
}

private fun endTurn(state: GameState): GameState {
    val enemyAtStart = state.enemy ?: return state.invalid("end_turn")
    if (state.screen != Screen.COMBAT) return state.invalid("end_turn")
    val retained = state.hand.filter { card ->
        val definition = GameCatalog.card(card.definitionId)
        val tags = if (card.upgraded) definition.upgradedTags else definition.tags
        CardTag.RETAIN in tags
    }
    val discarded = state.hand.filterNot { card -> retained.any { it.instanceId == card.instanceId } }
    var result = state.copy(
        hand = retained,
        discardPile = state.discardPile + discarded,
        stats = state.stats.copy(turns = state.stats.turns + 1),
    )
    result = EffectResolver.decayTurnStatuses(result, Side.PLAYER)
    result = EffectResolver.clearBlock(result, Side.ENEMY)
    result = EffectResolver.tickShock(result, Side.ENEMY)
    if ((result.enemy?.actor?.hp ?: 1) <= 0) return finishCombat(result)
    result = EffectResolver.resolveCard(result, enemyAtStart.intent, Side.ENEMY)
    if (result.player.hp <= 0) return defeat(result)
    result = EffectResolver.decayTurnStatuses(result, Side.ENEMY)
    val currentEnemy = result.enemy ?: return result.invalid("enemy_missing")
    val nextCursor = currentEnemy.intentCursor + 1
    result = result.copy(
        enemy = currentEnemy.copy(
            intentCursor = nextCursor,
            intent = EnemyAi.intent(currentEnemy.deck, nextCursor),
            turn = currentEnemy.turn + 1,
        ),
    )
    result = EffectResolver.clearBlock(result, Side.PLAYER)
    result = EffectResolver.tickShock(result, Side.PLAYER)
    if (result.player.hp <= 0) return defeat(result)
    val drawNeeded = (Balance.HAND_SIZE - result.hand.size).coerceAtLeast(0)
    result = EffectResolver.drawCards(result, drawNeeded)
    val nextTurn = result.turn + 1
    result = result.copy(
        energy = BossRules.playerEnergy(result, nextTurn),
        turn = nextTurn,
        events = result.events + GameEvent(GameEventKind.TURN_STARTED, amount = nextTurn, side = Side.PLAYER),
    )
    return BossRules.applyStartOfPlayerTurn(result)
}

private fun finishCombat(state: GameState): GameState {
    val eligible = GameCatalog.playerCards(state.heroClass)
    val shuffled = SplitMix64.shuffle(state.rngState, eligible)
    var nextId = state.nextInstanceId
    val draft = shuffled.value.take(Balance.DRAFT_SIZE).map { definition ->
        CardInstance(nextId++, definition.id)
    }
    return state.copy(
        rngState = shuffled.state,
        nextInstanceId = nextId,
        screen = Screen.DRAFT,
        enemy = null,
        drawPile = emptyList(),
        discardPile = emptyList(),
        exhaustPile = emptyList(),
        hand = emptyList(),
        draft = draft,
        stats = state.stats.copy(combatsWon = state.stats.combatsWon + 1),
        events = state.events + GameEvent(GameEventKind.VICTORY, side = Side.PLAYER),
    )
}

private fun chooseDraft(state: GameState, action: Action.ChooseDraft): GameState {
    if (state.screen != Screen.DRAFT || state.draft.size != Balance.DRAFT_SIZE) {
        return state.invalid(action.instanceId.toString())
    }
    val chosen = state.draft.firstOrNull { it.instanceId == action.instanceId }
        ?: return state.invalid(action.instanceId.toString())
    val denied = state.draft.filterNot { it.instanceId == chosen.instanceId }
    val drafted = state.copy(
        screen = Screen.MAP,
        playerDeck = state.playerDeck + chosen,
        enemyPool = state.enemyPool + denied,
        draft = emptyList(),
        nodeIndex = state.nodeIndex + 1,
        stats = state.stats.copy(draftsCompleted = state.stats.draftsCompleted + 1),
    )
    return RunMap.completeSelectedNode(drafted)
}

private fun navigate(state: GameState, destination: Screen): GameState {
    val permitted = setOf(Screen.DECK, Screen.SETTINGS, Screen.STATS)
    val returnScreen = if (state.enemy == null) Screen.MAP else Screen.COMBAT
    return when {
        destination in permitted -> state.copy(screen = destination)
        destination == returnScreen -> state.copy(screen = destination)
        state.runStatus == RunStatus.NOT_STARTED && destination == Screen.TITLE -> state.copy(screen = Screen.TITLE)
        else -> state.invalid(destination.name)
    }
}

private fun rest(state: GameState): GameState {
    if (state.runStatus != RunStatus.ACTIVE || state.screen != Screen.MAP || state.enemy != null) {
        return state.invalid("rest")
    }
    val amount = (state.player.maxHp * Balance.REST_HEAL_PERCENT) / 100
    val healed = minOf(state.player.maxHp, state.player.hp + amount)
    val rested = state.copy(
        player = state.player.copy(hp = healed),
        nodeIndex = state.nodeIndex + 1,
        events = state.events + GameEvent(GameEventKind.HEAL, "rest", healed - state.player.hp, Side.PLAYER),
    )
    return RunMap.completeSelectedNode(rested)
}

private fun completeAct(state: GameState): GameState {
    if (state.runStatus != RunStatus.ACTIVE || state.screen != Screen.MAP || state.enemy != null) {
        return state.invalid("complete_act")
    }
    return if (state.act >= 3) {
        state.copy(runStatus = RunStatus.WON, screen = Screen.RESULT)
    } else {
        RunMap.startAct(state.copy(act = state.act + 1, nodeIndex = 0))
    }
}

private fun abandon(state: GameState): GameState = if (state.runStatus == RunStatus.ACTIVE) {
    state.copy(runStatus = RunStatus.ABANDONED, screen = Screen.RESULT)
} else {
    state.invalid("abandon")
}

private fun defeat(state: GameState): GameState = state.copy(
    runStatus = RunStatus.LOST,
    screen = Screen.RESULT,
    events = state.events + GameEvent(GameEventKind.DEFEAT, side = Side.PLAYER),
)

private fun GameState.invalid(source: String): GameState = copy(
    events = events + GameEvent(GameEventKind.INVALID_ACTION, source),
)
