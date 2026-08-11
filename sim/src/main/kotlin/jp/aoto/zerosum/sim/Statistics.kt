package jp.aoto.zerosum.sim

import java.util.Locale

internal class Statistics {
    private val offers = mutableMapOf<String, Int>()
    private val winningOffers = mutableMapOf<String, Int>()
    private val picks = mutableMapOf<String, Int>()
    private val winningPicks = mutableMapOf<String, Int>()
    private var denialChoices: Int = 0
    private var totalDrafts: Int = 0
    private val summaries = mutableListOf<BotSummary>()

    internal fun recordBot(botName: String, results: List<RunResult>) {
        results.forEach { result ->
            result.drafts.forEach { draft ->
                totalDrafts++
                draft.offeredIds.forEach { id ->
                    offers[id] = offers.getOrDefault(id, 0) + 1
                    if (result.won) winningOffers[id] = winningOffers.getOrDefault(id, 0) + 1
                }
                picks[draft.chosenId] = picks.getOrDefault(draft.chosenId, 0) + 1
                if (result.won) winningPicks[draft.chosenId] = winningPicks.getOrDefault(draft.chosenId, 0) + 1
                if (draft.denialDecision) denialChoices++
            }
        }
        summaries += BotSummary(
            botName = botName,
            runs = results.size,
            wins = results.count(RunResult::won),
            totalTurns = results.sumOf { it.turns.toLong() },
            stuck = results.count(RunResult::stuck),
        )
    }

    internal fun botSummaries(): List<BotSummary> = summaries.toList()

    internal fun printCardMetrics() {
        val overall = summaries.sumOf(BotSummary::wins).toDouble() / summaries.sumOf(BotSummary::runs)
        val rows = offers.keys.map { id ->
            val offered = offers.getValue(id)
            val picked = picks.getOrDefault(id, 0)
            val pickRate = if (offered == 0) 0.0 else picked.toDouble() / offered
            val pickedWins = winningPicks.getOrDefault(id, 0)
            val pickedWinRate = if (picked == 0) overall else pickedWins.toDouble() / picked
            val notPicked = offered - picked
            val notPickedWins = winningOffers.getOrDefault(id, 0) - pickedWins
            val comparableRate = if (notPicked == 0) overall else notPickedWins.toDouble() / notPicked
            val standardError = kotlin.math.sqrt(
                pickedWinRate * (1.0 - pickedWinRate) / picked.coerceAtLeast(1) +
                    comparableRate * (1.0 - comparableRate) / notPicked.coerceAtLeast(1),
            )
            CardMetric(id, offered, picked, pickRate, pickedWinRate - comparableRate, standardError)
        }.sortedByDescending(CardMetric::pickRate)
        println("CARD_METRICS id offered picked adoption contribution")
        rows.forEach { row ->
            println("${row.id} ${row.offered} ${row.picked} ${pct(row.pickRate)} ${signedPct(row.contribution)}")
        }
        val outliers = rows.filter {
            it.picked >= 20 &&
                it.pickRate in 0.05..0.90 &&
                kotlin.math.abs(it.contribution) >= 0.15 &&
                // Ninety-nine percent confidence limits false positives across one hundred cards.
                kotlin.math.abs(it.contribution) >= 2.576 * it.standardError
        }
        println("OUTLIER_CARDS=${outliers.size} ${outliers.joinToString(",") { it.id }}")
        println("ZERO_ADOPTION=${rows.count { it.picked == 0 }}")
        println("OVER_90_ADOPTION=${rows.count { it.pickRate > 0.9 }}")
        val denialRate = if (totalDrafts == 0) 0.0 else denialChoices.toDouble() / totalDrafts
        println("DENIAL_DRAFT_RATE=${pct(denialRate)} ($denialChoices/$totalDrafts)")
    }

    private data class CardMetric(
        val id: String,
        val offered: Int,
        val picked: Int,
        val pickRate: Double,
        val contribution: Double,
        val standardError: Double,
    )
}

internal fun pct(value: Double): String = String.format(Locale.US, "%.2f%%", value * 100.0)
internal fun signedPct(value: Double): String = String.format(Locale.US, "%+.2fpt", value * 100.0)
