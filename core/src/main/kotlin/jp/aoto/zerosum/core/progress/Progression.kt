package jp.aoto.zerosum.core.progress

import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.RunStatus
import kotlinx.serialization.Serializable

/** Serializable aggregate kept locally across completed runs. */
@Serializable
public data class LifetimeStats(
    val runs: Int = 0,
    val wins: Int = 0,
    val totalTurns: Int = 0,
    val cardsPlayed: Int = 0,
    val damageDealt: Int = 0,
    val damageTaken: Int = 0,
    val cardsSeized: Int = 0,
    val combatsWon: Int = 0,
    val draftsCompleted: Int = 0,
    val highestAscensionWin: Int = -1,
    val dailyRuns: Int = 0,
) {
    /** Returns a new aggregate containing one terminal run. */
    public fun record(state: GameState, daily: Boolean): LifetimeStats = copy(
        runs = runs + 1,
        wins = wins + if (state.runStatus == RunStatus.WON) 1 else 0,
        totalTurns = totalTurns + state.stats.turns,
        cardsPlayed = cardsPlayed + state.stats.cardsPlayed,
        damageDealt = damageDealt + state.stats.damageDealt,
        damageTaken = damageTaken + state.stats.damageTaken,
        cardsSeized = cardsSeized + state.stats.cardsSeized,
        combatsWon = combatsWon + state.stats.combatsWon,
        draftsCompleted = draftsCompleted + state.stats.draftsCompleted,
        highestAscensionWin = if (state.runStatus == RunStatus.WON) maxOf(highestAscensionWin, state.ascension) else highestAscensionWin,
        dailyRuns = dailyRuns + if (daily || state.dailyChallenge) 1 else 0,
    )
}

/** Stable achievement metadata evaluated entirely in core. */
public data class AchievementDefinition(
    val id: String,
    val nameKey: String = "achievement_${id}_name",
    val descriptionKey: String = "achievement_${id}_description",
    val unlocked: (LifetimeStats) -> Boolean,
)

/** The complete thirty-achievement catalog. */
public object AchievementCatalog {
    private fun threshold(id: String, field: (LifetimeStats) -> Int, value: Int): AchievementDefinition =
        AchievementDefinition(id, unlocked = { field(it) >= value })

    private val definitions: List<AchievementDefinition> = listOf(
        threshold("first_run", LifetimeStats::runs, 1),
        threshold("persistent", LifetimeStats::runs, 5),
        threshold("regular", LifetimeStats::runs, 10),
        threshold("veteran", LifetimeStats::runs, 25),
        threshold("centurion", LifetimeStats::runs, 100),
        threshold("first_win", LifetimeStats::wins, 1),
        threshold("winning_line", LifetimeStats::wins, 3),
        threshold("dominant", LifetimeStats::wins, 10),
        threshold("unstoppable", LifetimeStats::wins, 25),
        threshold("legend", LifetimeStats::wins, 50),
        threshold("spark", LifetimeStats::damageDealt, 100),
        threshold("surge", LifetimeStats::damageDealt, 1_000),
        threshold("storm", LifetimeStats::damageDealt, 5_000),
        threshold("cataclysm", LifetimeStats::damageDealt, 20_000),
        threshold("card_student", LifetimeStats::cardsPlayed, 50),
        threshold("card_engine", LifetimeStats::cardsPlayed, 500),
        threshold("card_archive", LifetimeStats::cardsPlayed, 2_500),
        threshold("denial_one", LifetimeStats::cardsSeized, 1),
        threshold("denial_ten", LifetimeStats::cardsSeized, 10),
        threshold("denial_hundred", LifetimeStats::cardsSeized, 100),
        threshold("combatant", LifetimeStats::combatsWon, 10),
        threshold("elite_hunter", LifetimeStats::combatsWon, 50),
        threshold("grid_cleaner", LifetimeStats::combatsWon, 200),
        threshold("drafter", LifetimeStats::draftsCompleted, 10),
        threshold("draft_master", LifetimeStats::draftsCompleted, 100),
        threshold("endurance", LifetimeStats::totalTurns, 250),
        threshold("daily_signal", LifetimeStats::dailyRuns, 1),
        threshold("daily_regular", LifetimeStats::dailyRuns, 7),
        threshold("ascension_ten", { it.highestAscensionWin }, 10),
        threshold("zero_sum", { it.highestAscensionWin }, 20),
    )

    /** All definitions in stable display order. */
    public fun all(): List<AchievementDefinition> = definitions

    /** IDs unlocked by a lifetime snapshot. */
    public fun unlocked(stats: LifetimeStats): Set<String> =
        definitions.filter { it.unlocked(stats) }.mapTo(linkedSetOf()) { it.id }
}

/** Platform-independent conversion from ISO local date to a daily seed. */
public object DailyChallenge {
    /** Produces the same seed for every identical YYYY-MM-DD string. */
    public fun seed(isoDate: String): Long {
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(isoDate)) { "Date must use YYYY-MM-DD" }
        var hash = -3750763034362895579L
        isoDate.forEach { char -> hash = (hash xor char.code.toLong()) * 1099511628211L }
        return hash
    }
}
