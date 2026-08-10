package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.rng.SplitMix64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SplitMix64Test(private val seed: Long) {
    @Test
    fun sameSeedProducesSamePermutation() {
        val source = (0 until 32).toList()
        val first = SplitMix64.shuffle(seed, source)
        val second = SplitMix64.shuffle(seed, source)
        assertEquals(first, second)
        assertEquals(source, first.value.sorted())
        assertNotEquals(seed, first.state)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "seed={0}")
        fun seeds(): List<Array<Long>> = (0L until 64L).map { arrayOf(it) }
    }
}

class SplitMix64FixedTest {
    @Test
    fun seedZeroHasKnownFirstValue() {
        assertEquals(-2152535657050944081L, SplitMix64.nextLong(0L).value)
    }

    @Test
    fun boundedValuesStayInsideRange() {
        var state = 3L
        repeat(1_000) {
            val next = SplitMix64.nextInt(state, 7)
            assertTrue(next.value in 0..6)
            state = next.state
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroBoundIsRejected() {
        SplitMix64.nextInt(1L, 0)
    }
}

