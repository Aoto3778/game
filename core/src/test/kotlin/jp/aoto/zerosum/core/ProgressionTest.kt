package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.progress.AchievementCatalog
import jp.aoto.zerosum.core.progress.DailyChallenge
import jp.aoto.zerosum.core.progress.LifetimeStats
import jp.aoto.zerosum.core.save.GameStateJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class ProgressionTest {
    @Test public fun `achievement catalog has thirty unique entries`() {
        assertEquals(30, AchievementCatalog.all().size)
        assertEquals(30, AchievementCatalog.all().map { it.id }.toSet().size)
    }

    @Test public fun `fresh stats unlock nothing`() = assertTrue(AchievementCatalog.unlocked(LifetimeStats()).isEmpty())

    @Test public fun `one recorded run unlocks first run`() {
        val state = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, 1L)).copy(runStatus = RunStatus.LOST)
        assertTrue("first_run" in AchievementCatalog.unlocked(LifetimeStats().record(state, false)))
    }

    @Test public fun `highest ascension only records wins`() {
        val state = reduce(GameState(), Action.StartRun(HeroClass.BREAKER, 1L, 20))
        assertEquals(-1, LifetimeStats().record(state.copy(runStatus = RunStatus.LOST), false).highestAscensionWin)
        assertEquals(20, LifetimeStats().record(state.copy(runStatus = RunStatus.WON), false).highestAscensionWin)
    }

    @Test public fun `daily seed is deterministic`() = assertEquals(DailyChallenge.seed("2026-08-11"), DailyChallenge.seed("2026-08-11"))

    @Test public fun `daily seed changes by date`() = assertNotEquals(DailyChallenge.seed("2026-08-11"), DailyChallenge.seed("2026-08-12"))

    @Test(expected = IllegalArgumentException::class) public fun `daily seed rejects non iso date`() { DailyChallenge.seed("2026/08/11") }

    @Test public fun `lifetime stats serialize losslessly`() {
        val stats = LifetimeStats(runs = 4, wins = 2, cardsPlayed = 99, highestAscensionWin = 3)
        assertEquals(stats, GameStateJson.decodeStats(GameStateJson.encodeStats(stats)))
    }
}
