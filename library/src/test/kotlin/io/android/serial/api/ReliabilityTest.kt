package io.android.serial.api

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReliabilityTest {
    @Test
    fun matchesCommonResponseShapes() {
        assertTrue(ResponseMatchers.exact(byteArrayOf(1, 2)).matches(byteArrayOf(9), byteArrayOf(1, 2)))
        assertTrue(ResponseMatchers.prefix(byteArrayOf(1)).matches(byteArrayOf(9), byteArrayOf(1, 2)))
    }

    @Test
    fun calculatesBoundedBackoff() {
        val policy = ReconnectPolicy(true, 5, Duration.ofMillis(100), Duration.ofMillis(250))
        assertEquals(100, policy.delayFor(0).toMillis())
        assertEquals(200, policy.delayFor(1).toMillis())
        assertEquals(250, policy.delayFor(4).toMillis())
    }
}
