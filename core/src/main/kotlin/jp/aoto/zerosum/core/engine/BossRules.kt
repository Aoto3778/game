package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.model.BossRule
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.rng.SplitMix64

/** Rule replacements owned by the four bosses. */
public object BossRules {
    /** Applies any rule that fires after the player hand is drawn. */
    public fun applyStartOfPlayerTurn(state: GameState): GameState {
        val enemy = state.enemy ?: return state
        if (enemy.bossRule != BossRule.SYNCHRONIZER || state.hand.isEmpty()) return state
        val random = SplitMix64.nextInt(state.rngState, state.hand.size)
        val playerCard = state.hand[random.value]
        val exchangedHand = state.hand.map { card ->
            if (card.instanceId == playerCard.instanceId) enemy.intent else card
        }
        return state.copy(
            rngState = random.state,
            hand = exchangedHand,
            enemy = enemy.copy(intent = playerCard),
        )
    }

    /** Calculates Zero's shrinking energy budget for the given turn number. */
    public fun playerEnergy(state: GameState, nextTurn: Int): Int = when (state.enemy?.bossRule) {
        BossRule.ZERO -> (Balance.STARTING_ENERGY - (nextTurn - 1)).coerceAtLeast(0)
        else -> Balance.STARTING_ENERGY
    }
}
