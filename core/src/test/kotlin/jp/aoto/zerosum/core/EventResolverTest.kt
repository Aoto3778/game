package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class EventResolverTest(private val eventId: String) {
    @Test
    fun everyEventCanOpenInAnEligibleAct() {
        val event = GameCatalog.event(eventId)
        val run = reduce(GameState(), Action.StartRun(HeroClass.BREAKER, 4L)).copy(act = event.actMin)
        val opened = reduce(run, Action.BeginEvent(eventId))
        assertEquals(Screen.EVENT, opened.screen)
        assertEquals(eventId, opened.currentEventId)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun events(): List<Array<String>> = GameCatalog.allEvents().map { arrayOf(it.id) }
    }
}

class EventOutcomeTest {
    @Test
    fun eventChoiceAppliesTradeoffAndReturnsToMap() {
        val run = activeRun().copy(enemyPool = List(5) { CardInstance(100L + it, "strike") })
        val opened = reduce(run, Action.BeginEvent("pool_fire"))
        val result = reduce(opened, Action.ChooseEvent("fire_feed"))
        assertEquals(55, result.gold)
        assertEquals(1, result.enemyPool.size)
        assertEquals(Screen.MAP, result.screen)
    }

    @Test
    fun unaffordableChoiceIsRejectedWithoutMutation() {
        val opened = reduce(activeRun().copy(gold = 0), Action.BeginEvent("silent_auction"))
        val result = reduce(opened, Action.ChooseEvent("auction_bid"))
        assertEquals(opened.copy(events = emptyList()), result.copy(events = emptyList()))
        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun addRelicOutcomeAddsKnownRelicOnce() {
        val opened = reduce(activeRun(), Action.BeginEvent("sealed_locker"))
        val result = reduce(opened, Action.ChooseEvent("locker_force"))
        assertEquals(listOf("memory_clip"), result.relicIds)
    }

    private fun activeRun() = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, 3L))
}
