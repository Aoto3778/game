package jp.aoto.zerosum.sim

import java.util.Locale

/** Runs deterministic balance batches and prints acceptance metrics. */
fun main(args: Array<String>) {
    Locale.setDefault(Locale.US)
    val config = parseArgs(args)
    println("ZERO SUM deterministic balance simulation")
    println("runs_per_bot=${config.runs} seed=${config.seed} ascension=${config.ascension}")
    val statistics = Statistics()
    val normalResults = mutableMapOf<String, List<RunResult>>()
    standardBots.forEachIndexed { botIndex, bot ->
        val results = List(config.runs) { runIndex ->
            val seed = config.seed + botIndex * 1_000_003L + runIndex
            RunSimulator.run(bot, seed, config.ascension, denialEnabled = true)
        }
        normalResults[bot.name] = results
        statistics.recordBot(bot.name, results)
        val summary = statistics.botSummaries().last()
        println(
            "BOT=${summary.botName} wins=${summary.wins}/${summary.runs} " +
                "win_rate=${pct(summary.winRate)} avg_turns=${String.format(Locale.US, "%.2f", summary.averageTurns)} " +
                "stuck=${summary.stuck}/${summary.runs}",
        )
        println(
            "REACHED_ACT bot=${bot.name} " +
                HeroActSummary.format(results),
        )
    }
    val targetPassed = statistics.botSummaries().all { it.winRate in 0.35..0.60 }
    println("BOT_WIN_RATE_TARGET=${if (targetPassed) "PASS" else "FAIL"} expected=35%-60%")

    val controlRuns = maxOf(200, config.runs / 2)
    val normalSynergy = normalResults.getValue(SynergyBot.name).take(controlRuns)
    val synergyOffset = standardBots.indexOf(SynergyBot) * 1_000_003L
    val noDenial = List(controlRuns) { runIndex ->
        RunSimulator.run(SynergyBot, config.seed + synergyOffset + runIndex, config.ascension, denialEnabled = false)
    }
    val normalRate = normalSynergy.count(RunResult::won).toDouble() / normalSynergy.size
    val controlRate = noDenial.count(RunResult::won).toDouble() / noDenial.size
    println("DENIAL_CONTROL enabled=${pct(normalRate)} disabled=${pct(controlRate)} lift=${signedPct(normalRate - controlRate)}")

    val difficultyRuns = maxOf(200, config.runs / 4)
    val ascensionZero = mutableListOf<RunResult>()
    val ascensionTwenty = mutableListOf<RunResult>()
    standardBots.forEachIndexed { botIndex, bot ->
        repeat(difficultyRuns) { runIndex ->
            val seed = config.seed + 20_000_000L + botIndex * 100_003L + runIndex
            ascensionZero += RunSimulator.run(bot, seed, 0, denialEnabled = true)
            ascensionTwenty += RunSimulator.run(bot, seed, 20, denialEnabled = true)
        }
    }
    val zeroRate = ascensionZero.count(RunResult::won).toDouble() / ascensionZero.size
    val twentyRate = ascensionTwenty.count(RunResult::won).toDouble() / ascensionTwenty.size
    println("ASCENSION_GAP a0=${pct(zeroRate)} a20=${pct(twentyRate)} gap=${signedPct(zeroRate - twentyRate)}")
    println("ASCENSION_GAP_TARGET=${if (zeroRate - twentyRate >= 0.20) "PASS" else "FAIL"} expected>=20pt")

    statistics.printCardMetrics()
}

private object HeroActSummary {
    fun format(results: List<RunResult>): String = (1..3).joinToString(" ") { act ->
        "act$act=${results.count { it.reachedAct == act }}"
    }
}
