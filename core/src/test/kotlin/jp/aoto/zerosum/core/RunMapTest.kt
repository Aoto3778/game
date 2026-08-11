package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.engine.RunMap
import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.MapNodeType
import jp.aoto.zerosum.core.model.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class RunMapTest {
    @Test
    public fun `acts contain twelve to sixteen nodes`() {
        (1..3).forEach { act -> assertTrue(RunMap.generate(5L, act).size in 12..16) }
    }

    @Test
    public fun `same seed creates same map`() {
        assertEquals(RunMap.generate(77L, 2), RunMap.generate(77L, 2))
    }

    @Test
    public fun `different seeds change content`() {
        assertNotEquals(
            RunMap.generate(77L, 2).map { it.contentId },
            RunMap.generate(78L, 2).map { it.contentId },
        )
    }

    @Test
    public fun `new run exposes first row only`() {
        val state = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, 9L))
        assertEquals(state.mapNodes.filter { it.row == 0 }.map { it.id }, state.availableNodeIds)
    }

    @Test
    public fun `unreachable node is rejected`() {
        val state = reduce(GameState(), Action.StartRun(HeroClass.BREAKER, 9L))
        val locked = state.mapNodes.first { it.row == 2 }
        val result = reduce(state, Action.SelectMapNode(locked.id))
        assertEquals(GameEventKind.INVALID_ACTION, result.events.single().kind)
    }

    @Test
    public fun `normal selection enters combat`() {
        val state = reduce(GameState(), Action.StartRun(HeroClass.RESOLVER, 3L))
        val normal = state.mapNodes.first { it.id in state.availableNodeIds }
        val result = reduce(state, Action.SelectMapNode(normal.id))
        assertEquals(Screen.COMBAT, result.screen)
        assertEquals(normal.contentId, result.enemy?.definitionId)
    }

    @Test
    public fun `rest immediately unlocks successors`() {
        val initial = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, 3L))
        val rest = initial.mapNodes.first { it.type == MapNodeType.REST }
        val positioned = initial.copy(availableNodeIds = listOf(rest.id), player = initial.player.copy(hp = 1))
        val result = reduce(positioned, Action.SelectMapNode(rest.id))
        assertEquals(rest.connections, result.availableNodeIds)
        assertTrue(result.player.hp > 1)
    }

    @Test
    public fun `third act includes mirror then zero`() {
        val bosses = RunMap.generate(1L, 3).filter { it.type == MapNodeType.BOSS }
        assertEquals(listOf("mirror", "zero"), bosses.map { it.contentId })
        assertEquals(listOf(bosses.last().id), bosses.first().connections)
    }
}
