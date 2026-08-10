package jp.aoto.zerosum.core.rng

/** A value paired with the next immutable random state. */
public data class RandomResult<T>(val value: T, val state: Long)

/** Small deterministic generator used instead of global random state. */
public object SplitMix64 {
    private const val GAMMA: Long = -7046029254386353131L
    private const val MIX_1: Long = -4658895280553007687L
    private const val MIX_2: Long = -7723592293110705685L

    /** Produces one 64-bit value and the next state. */
    public fun nextLong(state: Long): RandomResult<Long> {
        val nextState = state + GAMMA
        var value = nextState
        value = (value xor (value ushr 30)) * MIX_1
        value = (value xor (value ushr 27)) * MIX_2
        value = value xor (value ushr 31)
        return RandomResult(value, nextState)
    }

    /** Produces a value in `[0, bound)` without mutable global state. */
    public fun nextInt(state: Long, bound: Int): RandomResult<Int> {
        require(bound > 0) { "bound must be positive: $bound" }
        val next = nextLong(state)
        val value = ((next.value ushr 1) % bound.toLong()).toInt()
        return RandomResult(value, next.state)
    }

    /** Fisher-Yates shuffle that returns a new list and final RNG state. */
    public fun <T> shuffle(state: Long, source: List<T>): RandomResult<List<T>> {
        val result = source.toMutableList()
        var randomState = state
        for (index in result.lastIndex downTo 1) {
            val next = nextInt(randomState, index + 1)
            randomState = next.state
            val other = next.value
            val value = result[index]
            result[index] = result[other]
            result[other] = value
        }
        return RandomResult(result.toList(), randomState)
    }
}

