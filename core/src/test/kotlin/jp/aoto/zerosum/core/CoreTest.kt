package jp.aoto.zerosum.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CoreTest {
    @Test
    fun moduleNameIsStable() {
        assertEquals("ZERO SUM", Core.NAME)
    }
}

