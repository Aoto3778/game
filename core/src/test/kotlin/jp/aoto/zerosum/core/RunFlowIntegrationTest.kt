package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.EventRequirement
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** End-to-end reducer flow used as the UI's emulator-free playthrough contract. */
public class RunFlowIntegrationTest {
    @Test
    public fun `reachable nodes complete a full three act run`() {
        var state = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, 20260811L))
        var safety = 0
        while (state.runStatus == RunStatus.ACTIVE && safety++ < 100) {
            state = when (state.screen) {
                Screen.MAP -> reduce(state, Action.SelectMapNode(state.availableNodeIds.first()))
                Screen.COMBAT -> winCurrentCombat(state)
                Screen.DRAFT -> reduce(state, Action.ChooseDraft(state.draft.first().instanceId))
                Screen.EVENT -> chooseLegalEventBranch(state)
                else -> state
            }
        }
        assertEquals(RunStatus.WON, state.runStatus)
        assertEquals(Screen.RESULT, state.screen)
        assertEquals(3, state.act)
        assertTrue(state.stats.combatsWon >= 6)
    }

    private fun winCurrentCombat(state: GameState): GameState {
        val enemy = requireNotNull(state.enemy)
        val strike = CardInstance(state.nextInstanceId, "strike")
        val prepared = state.copy(
            nextInstanceId = state.nextInstanceId + 1,
            energy = 3,
            hand = listOf(strike),
            enemy = enemy.copy(actor = enemy.actor.copy(hp = 1)),
        )
        return reduce(prepared, Action.PlayCard(strike.instanceId))
    }

    private fun chooseLegalEventBranch(state: GameState): GameState {
        val event = GameCatalog.event(requireNotNull(state.currentEventId))
        val choice = event.choices.first { choice ->
            when (choice.requirement) {
                EventRequirement.NONE -> true
                EventRequirement.MIN_HP -> state.player.hp >= choice.requirementAmount
                EventRequirement.MIN_GOLD -> state.gold >= choice.requirementAmount
                EventRequirement.MIN_POOL -> state.enemyPool.size >= choice.requirementAmount
                EventRequirement.MAX_POOL -> state.enemyPool.size <= choice.requirementAmount
            }
        }
        return reduce(state, Action.ChooseEvent(choice.id))
    }
}
