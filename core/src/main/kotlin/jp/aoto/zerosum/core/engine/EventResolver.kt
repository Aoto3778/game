package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.EventChoice
import jp.aoto.zerosum.core.model.EventOutcome
import jp.aoto.zerosum.core.model.EventOutcomeKind
import jp.aoto.zerosum.core.model.EventRequirement
import jp.aoto.zerosum.core.model.GameEvent
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.core.model.Side
import jp.aoto.zerosum.core.rng.SplitMix64

/** Pure resolution of branching event costs and rewards. */
public object EventResolver {
    /** Opens an event that is legal in the current act. */
    public fun begin(state: GameState, eventId: String): GameState {
        if (state.runStatus != RunStatus.ACTIVE || state.enemy != null || state.currentEventId != null) {
            return state.invalidEvent(eventId)
        }
        val definition = GameCatalog.allEvents().firstOrNull { it.id == eventId }
            ?: return state.invalidEvent(eventId)
        if (state.act !in definition.actMin..definition.actMax) return state.invalidEvent(eventId)
        return state.copy(screen = Screen.EVENT, currentEventId = eventId)
    }

    /** Applies one legal choice in declaration order and returns to the map. */
    public fun choose(state: GameState, choiceId: String): GameState {
        val eventId = state.currentEventId ?: return state.invalidEvent(choiceId)
        val definition = GameCatalog.event(eventId)
        val choice = definition.choices.firstOrNull { it.id == choiceId }
            ?: return state.invalidEvent(choiceId)
        if (!requirementMet(state, choice)) return state.invalidEvent(choiceId)
        var result = state
        choice.outcomes.forEach { outcome -> result = applyOutcome(result, outcome) }
        val finished = if (result.player.hp <= 0) {
            result.copy(runStatus = RunStatus.LOST, screen = Screen.RESULT)
        } else {
            result.copy(screen = Screen.MAP, nodeIndex = result.nodeIndex + 1)
        }
        val recorded = finished.copy(
            currentEventId = null,
            events = finished.events + GameEvent(GameEventKind.EVENT_RESOLVED, "$eventId:$choiceId", side = Side.PLAYER),
        )
        return RunMap.completeSelectedNode(recorded)
    }

    private fun requirementMet(state: GameState, choice: EventChoice): Boolean = when (choice.requirement) {
        EventRequirement.NONE -> true
        EventRequirement.MIN_HP -> state.player.hp >= choice.requirementAmount
        EventRequirement.MIN_GOLD -> state.gold >= choice.requirementAmount
        EventRequirement.MIN_POOL -> state.enemyPool.size >= choice.requirementAmount
        EventRequirement.MAX_POOL -> state.enemyPool.size <= choice.requirementAmount
    }

    private fun applyOutcome(state: GameState, outcome: EventOutcome): GameState = when (outcome.kind) {
        EventOutcomeKind.HP_DELTA -> state.copy(
            player = state.player.copy(hp = (state.player.hp + outcome.amount).coerceIn(0, state.player.maxHp)),
        )
        EventOutcomeKind.MAX_HP_DELTA -> changeMaxHp(state, outcome.amount)
        EventOutcomeKind.GOLD_DELTA -> state.copy(gold = (state.gold + outcome.amount).coerceAtLeast(0))
        EventOutcomeKind.UPGRADE_RANDOM_CARD -> upgradeRandom(state, outcome.amount)
        EventOutcomeKind.REMOVE_RANDOM_CARD -> removeRandom(state, outcome.amount)
        EventOutcomeKind.ADD_CARD -> addCard(state, outcome.contentId)
        EventOutcomeKind.BURN_POOL -> burnPool(state, outcome.amount)
        EventOutcomeKind.ADD_RELIC -> addRelic(state, outcome.contentId)
        EventOutcomeKind.NOTHING -> state
    }

    private fun changeMaxHp(state: GameState, amount: Int): GameState {
        val newMax = (state.player.maxHp + amount).coerceAtLeast(1)
        val healedHp = if (amount > 0) state.player.hp + amount else state.player.hp
        return state.copy(player = state.player.copy(hp = healedHp.coerceAtMost(newMax), maxHp = newMax))
    }

    private fun upgradeRandom(state: GameState, amount: Int): GameState {
        var result = state
        repeat(amount) {
            val candidates = result.playerDeck.filterNot(CardInstance::upgraded)
            if (candidates.isEmpty()) return@repeat
            val random = SplitMix64.nextInt(result.rngState, candidates.size)
            val chosen = candidates[random.value]
            result = result.copy(
                rngState = random.state,
                playerDeck = result.playerDeck.map { if (it.instanceId == chosen.instanceId) it.copy(upgraded = true) else it },
            )
        }
        return result
    }

    private fun removeRandom(state: GameState, amount: Int): GameState {
        var result = state
        repeat(amount) {
            if (result.playerDeck.size <= 1) return@repeat
            val random = SplitMix64.nextInt(result.rngState, result.playerDeck.size)
            val removed = result.playerDeck[random.value]
            result = result.copy(
                rngState = random.state,
                playerDeck = result.playerDeck.filterNot { it.instanceId == removed.instanceId },
            )
        }
        return result
    }

    private fun addCard(state: GameState, contentId: String?): GameState {
        val id = contentId ?: return state
        if (GameCatalog.allCards().none { it.id == id }) return state
        val card = CardInstance(state.nextInstanceId, id)
        return state.copy(nextInstanceId = state.nextInstanceId + 1, playerDeck = state.playerDeck + card)
    }

    private fun burnPool(state: GameState, amount: Int): GameState {
        var result = state
        repeat(amount) {
            if (result.enemyPool.isEmpty()) return@repeat
            val random = SplitMix64.nextInt(result.rngState, result.enemyPool.size)
            val burned = result.enemyPool[random.value]
            result = result.copy(
                rngState = random.state,
                enemyPool = result.enemyPool.filterNot { it.instanceId == burned.instanceId },
                stats = result.stats.copy(cardsSeized = result.stats.cardsSeized + 1),
            )
        }
        return result
    }

    private fun addRelic(state: GameState, contentId: String?): GameState {
        val id = contentId ?: return state
        if (GameCatalog.allRelics().none { it.id == id } || id in state.relicIds) return state
        return state.copy(relicIds = state.relicIds + id)
    }

    private fun GameState.invalidEvent(source: String): GameState = copy(
        events = events + GameEvent(GameEventKind.INVALID_ACTION, source),
    )
}
