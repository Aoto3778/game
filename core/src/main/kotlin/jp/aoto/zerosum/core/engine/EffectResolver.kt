package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.CardDefinition
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.BossRule
import jp.aoto.zerosum.core.model.CombatantState
import jp.aoto.zerosum.core.model.Effect
import jp.aoto.zerosum.core.model.EffectCondition
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.EffectTarget
import jp.aoto.zerosum.core.model.GameEvent
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Side
import jp.aoto.zerosum.core.model.Status
import jp.aoto.zerosum.core.rng.SplitMix64

/** Ordered, side-effect-free resolution of card primitives. */
public object EffectResolver {
    /** Draws cards for player-owned effects or turn setup. */
    public fun drawCards(state: GameState, amount: Int): GameState = draw(state, Side.PLAYER, amount)

    /** Resolves every effect on a card in declaration order. */
    public fun resolveCard(state: GameState, card: CardInstance, side: Side): GameState {
        val definition = GameCatalog.card(card.definitionId)
        val effects = if (card.upgraded) definition.upgradedEffects else definition.effects
        var result = state.copy(
            events = state.events + GameEvent(GameEventKind.CARD_PLAYED, definition.id, side = side),
        )
        effects.forEach { effect ->
            if (conditionMet(result, effect, side)) {
                result = if (isInterrupted(result, effect, side)) {
                    consumeInterrupt(result, targetSide(effect, side))
                } else {
                    resolveEffect(result, effect, side, definition)
                }
            }
        }
        return result
    }

    /** Applies start-of-turn Shock damage and halves its stacks. */
    public fun tickShock(state: GameState, side: Side): GameState {
        val stacks = actor(state, side)?.status(Status.SHOCK) ?: return state
        if (stacks <= 0) return state
        val damaged = dealDamage(state, side.opponent(), side, stacks, 1, sourceId = "status_shock", includeOutput = false)
        return updateActor(damaged, side) { combatant ->
            combatant.withStatus(Status.SHOCK, stacks / 2)
        }
    }

    /** Decrements Output and Overload at the end of the owning side's turn. */
    public fun decayTurnStatuses(state: GameState, side: Side): GameState = updateActor(state, side) { combatant ->
        combatant
            .withStatus(Status.OUTPUT, (combatant.status(Status.OUTPUT) - 1).coerceAtLeast(0))
            .withStatus(Status.OVERLOAD, (combatant.status(Status.OVERLOAD) - 1).coerceAtLeast(0))
    }

    /** Clears temporary block after it has protected its owner for one opposing turn. */
    public fun clearBlock(state: GameState, side: Side): GameState = updateActor(state, side) { it.copy(block = 0) }

    private fun resolveEffect(
        state: GameState,
        effect: Effect,
        side: Side,
        definition: CardDefinition,
    ): GameState {
        val bossBonus = if (
            side == Side.ENEMY &&
            effect.kind == EffectKind.DAMAGE &&
            state.enemy?.bossRule == BossRule.ACCUMULATOR
        ) {
            state.enemyPool.size / 4
        } else {
            0
        }
        val amount = (scaledAmount(state, effect, side) + bossBonus).coerceAtLeast(0)
        val target = targetSide(effect, side)
        return when (effect.kind) {
            EffectKind.DAMAGE -> dealDamage(state, side, target, amount, effect.hits, definition.id)
            EffectKind.BLOCK -> addBlock(state, target, amount, definition.id)
            EffectKind.DRAW -> draw(state, side, amount)
            EffectKind.APPLY_STATUS -> applyStatus(state, target, effect, amount, definition.id)
            EffectKind.GAIN_ENERGY -> if (side == Side.PLAYER) state.copy(energy = state.energy + amount) else state
            EffectKind.HEAL -> heal(state, target, amount, definition.id)
            EffectKind.SEIZE -> seize(state, amount)
            EffectKind.DISCARD -> discardRandom(state, side, amount)
            EffectKind.RETURN_DISCARD -> returnDiscard(state, side, amount)
            EffectKind.COPY_LAST -> copyLast(state, side, amount)
            EffectKind.GAIN_MAX_HP -> gainMaxHp(state, target, amount)
        }
    }

    private fun scaledAmount(state: GameState, effect: Effect, side: Side): Int {
        val self = actor(state, side) ?: return effect.amount
        val opponent = actor(state, side.opponent())
        val bonus = when (effect.scaling) {
            Scaling.NONE -> 0
            Scaling.OUTPUT -> self.status(Status.OUTPUT)
            Scaling.SELF_BLOCK -> self.block
            Scaling.OPPONENT_SHOCK -> opponent?.status(Status.SHOCK) ?: 0
            Scaling.ENEMY_POOL -> state.enemyPool.size
            Scaling.HAND_SIZE -> state.hand.size
            Scaling.MISSING_HP -> self.maxHp - self.hp
        }
        return effect.amount + bonus
    }

    private fun conditionMet(state: GameState, effect: Effect, side: Side): Boolean {
        val self = actor(state, side) ?: return false
        val opponent = actor(state, side.opponent())
        return when (effect.condition) {
            EffectCondition.ALWAYS -> true
            EffectCondition.IF_BLOCKED -> self.block > 0
            EffectCondition.IF_SHOCKED -> (opponent?.status(Status.SHOCK) ?: 0) > 0
            EffectCondition.IF_LOW_HP -> self.hp * 100 <= self.maxHp * effect.threshold.coerceAtLeast(1)
            EffectCondition.IF_HAND_AT_LEAST -> state.hand.size >= effect.threshold
            EffectCondition.IF_ENEMY_POOL_AT_LEAST -> state.enemyPool.size >= effect.threshold
        }
    }

    private fun isInterrupted(state: GameState, effect: Effect, side: Side): Boolean {
        if (effect.target != EffectTarget.OPPONENT) return false
        return (actor(state, side.opponent())?.status(Status.INTERRUPT) ?: 0) > 0
    }

    private fun consumeInterrupt(state: GameState, side: Side): GameState = updateActor(state, side) { combatant ->
        combatant.withStatus(Status.INTERRUPT, combatant.status(Status.INTERRUPT) - 1)
    }

    private fun dealDamage(
        state: GameState,
        source: Side,
        target: Side,
        baseAmount: Int,
        hits: Int,
        sourceId: String,
        includeOutput: Boolean = true,
    ): GameState {
        var result = state
        repeat(hits.coerceAtLeast(1)) {
            val sourceActor = actor(result, source)
            val targetActor = actor(result, target) ?: return@repeat
            val output = if (includeOutput) sourceActor?.status(Status.OUTPUT) ?: 0 else 0
            val raw = (baseAmount + output).coerceAtLeast(0)
            val overload = targetActor.status(Status.OVERLOAD)
            val amplified = (raw * (100 + Balance.OVERLOAD_PERCENT_PER_STACK * overload) + 99) / 100
            val absorbed = minOf(targetActor.block, amplified)
            val hpDamage = (amplified - absorbed).coerceAtLeast(0)
            val updated = targetActor.copy(
                hp = (targetActor.hp - hpDamage).coerceAtLeast(0),
                block = targetActor.block - absorbed,
            )
            result = updateActor(result, target) { updated }
            val stats = if (source == Side.PLAYER) {
                result.stats.copy(damageDealt = result.stats.damageDealt + hpDamage)
            } else {
                result.stats.copy(damageTaken = result.stats.damageTaken + hpDamage)
            }
            result = result.copy(
                stats = stats,
                events = result.events + GameEvent(GameEventKind.DAMAGE, sourceId, amplified, target),
            )
        }
        return result
    }

    private fun addBlock(state: GameState, target: Side, amount: Int, sourceId: String): GameState {
        val updated = updateActor(state, target) { it.copy(block = it.block + amount) }
        return updated.copy(events = updated.events + GameEvent(GameEventKind.BLOCK, sourceId, amount, target))
    }

    private fun applyStatus(
        state: GameState,
        target: Side,
        effect: Effect,
        amount: Int,
        sourceId: String,
    ): GameState {
        val status = effect.status ?: return state
        val updated = updateActor(state, target) { combatant ->
            combatant.withStatus(status, combatant.status(status) + amount)
        }
        return updated.copy(events = updated.events + GameEvent(GameEventKind.STATUS, sourceId, amount, target))
    }

    private fun heal(state: GameState, target: Side, amount: Int, sourceId: String): GameState {
        val before = actor(state, target) ?: return state
        val healed = minOf(amount, before.maxHp - before.hp)
        val updated = updateActor(state, target) { it.copy(hp = it.hp + healed) }
        return updated.copy(events = updated.events + GameEvent(GameEventKind.HEAL, sourceId, healed, target))
    }

    private fun gainMaxHp(state: GameState, target: Side, amount: Int): GameState = updateActor(state, target) {
        it.copy(hp = it.hp + amount, maxHp = it.maxHp + amount)
    }

    private fun draw(state: GameState, side: Side, amount: Int): GameState {
        if (side != Side.PLAYER) return state
        var result = state
        repeat(amount) {
            if (result.drawPile.isEmpty() && result.discardPile.isNotEmpty()) {
                val shuffled = SplitMix64.shuffle(result.rngState, result.discardPile)
                result = result.copy(rngState = shuffled.state, drawPile = shuffled.value, discardPile = emptyList())
            }
            val drawn = result.drawPile.firstOrNull() ?: return@repeat
            result = result.copy(
                drawPile = result.drawPile.drop(1),
                hand = result.hand + drawn,
                events = result.events + GameEvent(GameEventKind.DRAW, drawn.definitionId, side = Side.PLAYER),
            )
        }
        return result
    }

    private fun discardRandom(state: GameState, side: Side, amount: Int): GameState {
        if (side != Side.PLAYER) return state
        var result = state
        repeat(amount) {
            if (result.hand.isEmpty()) return@repeat
            val random = SplitMix64.nextInt(result.rngState, result.hand.size)
            val card = result.hand[random.value]
            result = result.copy(
                rngState = random.state,
                hand = result.hand.filterNot { it.instanceId == card.instanceId },
                discardPile = result.discardPile + card,
                events = result.events + GameEvent(GameEventKind.DISCARD, card.definitionId, side = Side.PLAYER),
            )
        }
        return result
    }

    private fun returnDiscard(state: GameState, side: Side, amount: Int): GameState {
        if (side != Side.PLAYER) return state
        val returned = state.discardPile.takeLast(amount)
        return state.copy(
            discardPile = state.discardPile.dropLast(returned.size),
            hand = state.hand + returned,
        )
    }

    private fun copyLast(state: GameState, side: Side, amount: Int): GameState {
        if (side != Side.PLAYER) return state
        val definitionId = state.lastPlayedDefinitionId ?: return state
        val copies = List(amount) { offset ->
            CardInstance(state.nextInstanceId + offset, definitionId)
        }
        return state.copy(
            nextInstanceId = state.nextInstanceId + copies.size,
            hand = state.hand + copies,
        )
    }

    private fun seize(state: GameState, amount: Int): GameState {
        var result = state
        repeat(amount) {
            if (result.enemyPool.isEmpty()) return@repeat
            val random = SplitMix64.nextInt(result.rngState, result.enemyPool.size)
            val burned = result.enemyPool[random.value]
            result = result.copy(
                rngState = random.state,
                enemyPool = result.enemyPool.filterNot { it.instanceId == burned.instanceId },
                events = result.events + GameEvent(GameEventKind.SEIZE, burned.definitionId, side = Side.PLAYER),
                stats = result.stats.copy(cardsSeized = result.stats.cardsSeized + 1),
            )
        }
        return result
    }

    private fun actor(state: GameState, side: Side): CombatantState? = when (side) {
        Side.PLAYER -> state.player
        Side.ENEMY -> state.enemy?.actor
    }

    private fun updateActor(
        state: GameState,
        side: Side,
        transform: (CombatantState) -> CombatantState,
    ): GameState = when (side) {
        Side.PLAYER -> state.copy(player = transform(state.player))
        Side.ENEMY -> {
            val enemy = state.enemy ?: return state
            state.copy(enemy = enemy.copy(actor = transform(enemy.actor)))
        }
    }

    private fun targetSide(effect: Effect, source: Side): Side = when (effect.target) {
        EffectTarget.SELF -> source
        EffectTarget.OPPONENT -> source.opponent()
    }

    private fun CombatantState.withStatus(status: Status, amount: Int): CombatantState {
        val updated = statuses.toMutableMap()
        if (amount <= 0) updated.remove(status) else updated[status] = amount
        return copy(statuses = updated.toMap())
    }

    private fun Side.opponent(): Side = if (this == Side.PLAYER) Side.ENEMY else Side.PLAYER
}
