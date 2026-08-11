package jp.aoto.zerosum.sim

internal data class SimArgs(
    val runs: Int = 500,
    val seed: Long = 1L,
    val ascension: Int = 0,
)

internal fun parseArgs(args: Array<String>): SimArgs {
    var runs = 500
    var seed = 1L
    var ascension = 0
    var index = 0
    while (index < args.size) {
        when (args[index]) {
            "--runs" -> runs = args.getOrNull(++index)?.toIntOrNull() ?: error("--runs requires an integer")
            "--seed" -> seed = args.getOrNull(++index)?.toLongOrNull() ?: error("--seed requires a long")
            "--ascension" -> ascension = args.getOrNull(++index)?.toIntOrNull() ?: error("--ascension requires an integer")
            else -> error("Unknown argument: ${args[index]}")
        }
        index++
    }
    require(runs > 0) { "runs must be positive" }
    require(ascension in 0..20) { "ascension must be in 0..20" }
    return SimArgs(runs, seed, ascension)
}

