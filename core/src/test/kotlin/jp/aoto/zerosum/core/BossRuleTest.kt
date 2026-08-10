package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BossRuleTest {
    @Test
    fun synchronizerExchangesPublishedIntentWithOneOpeningCard() {
        val combat = begin("synchronizer")
        assertTrue(combat.hand.any { it.definitionId == "enemy_jab" })
        assertTrue(requireNotNull(combat.enemy).intent.definitionId in setOf("strike", "guard", "cycle"))
    }

    @Test
    fun accumulatorGainsOneDamagePerFourPoolCards() {
        val pool = List(8) { index -> CardInstance(100L + index, "strike") }
        val combat = begin("accumulator", pool).let { state ->
            val enemy = requireNotNull(state.enemy)
            state.copy(enemy = enemy.copy(intent = CardInstance(999L, "enemy_jab")))
        }
        assertEquals(64, reduce(combat, Action.EndTurn).player.hp)
    }

    @Test
    fun mirrorUsesAFullCopyOfPlayerDeck() {
        val combat = begin("mirror")
        val enemyCards = requireNotNull(combat.enemy).deck.map { it.definitionId }.sorted()
        assertEquals(combat.playerDeck.map { it.definitionId }.sorted(), enemyCards)
    }

    @Test
    fun zeroReducesEnergyEveryTurnToZero() {
        var combat = begin("zero")
        combat = reduce(combat, Action.EndTurn)
        assertEquals(2, combat.energy)
        combat = reduce(combat, Action.EndTurn)
        assertEquals(1, combat.energy)
        combat = reduce(combat, Action.EndTurn)
        assertEquals(0, combat.energy)
    }

    private fun begin(enemyId: String, pool: List<CardInstance> = emptyList()): GameState {
        val run = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, 12L)).copy(enemyPool = pool)
        return reduce(run, Action.BeginCombat(enemyId))
    }
}

