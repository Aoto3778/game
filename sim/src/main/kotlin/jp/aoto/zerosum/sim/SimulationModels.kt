package jp.aoto.zerosum.sim

internal data class DraftRecord(
    val offeredIds: List<String>,
    val chosenId: String,
    val denialDecision: Boolean,
)

internal data class RunResult(
    val won: Boolean,
    val turns: Int,
    val stuck: Boolean,
    val drafts: List<DraftRecord>,
    val reachedAct: Int,
    val remainingHp: Int,
)

internal data class BotSummary(
    val botName: String,
    val runs: Int,
    val wins: Int,
    val totalTurns: Long,
    val stuck: Int,
) {
    val winRate: Double get() = wins.toDouble() / runs
    val averageTurns: Double get() = totalTurns.toDouble() / runs
}
