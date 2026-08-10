package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.CombatantState
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass

internal fun combatState(seed: Long = 1L): GameState {
    val run = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, seed))
    return reduce(run, Action.BeginCombat("training_drone"))
}

internal fun stateWithCard(
    cardId: String,
    upgraded: Boolean = false,
    seed: Long = 1L,
): Pair<GameState, CardInstance> {
    val base = combatState(seed)
    val card = CardInstance(9_999L, cardId, upgraded)
    val enemy = requireNotNull(base.enemy) { "combat fixture requires an enemy" }
    return base.copy(
        energy = 99,
        hand = listOf(card),
        drawPile = emptyList(),
        discardPile = emptyList(),
        enemy = enemy.copy(actor = CombatantState(hp = 200, maxHp = 200)),
        events = emptyList(),
    ) to card
}

