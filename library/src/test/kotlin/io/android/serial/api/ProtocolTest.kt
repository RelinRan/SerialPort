package io.android.serial.api

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import java.nio.ByteOrder

class ProtocolTest {
    @Test
    fun calculatesCommonChecksums() {
        val data = byteArrayOf(1, 2, 3)
        assertContentEquals(byteArrayOf(0), data.withChecksum(Checksums.Xor8).takeLast(1).toByteArray())
        assertContentEquals(byteArrayOf(6), Checksums.Sum8.calculate(data))
        assertContentEquals(byteArrayOf(0x61, 0x61), Checksums.Crc16Modbus.calculate(data))
    }

    @Test
    fun parsesFragmentedAndCoalescedFrames() {
        val parser = SerialFrameParser(byteArrayOf(0x55, 0xAA.toByte()), lengthOffset = 2)
        val frames = parser.offer(byteArrayOf(0x00, 0x55, 0xAA.toByte(), 0x02, 0x10)) +
            parser.offer(byteArrayOf(0x20, 0x55, 0xAA.toByte(), 0x01, 0x7F))
        assertEquals(2, frames.size)
        assertContentEquals(byteArrayOf(0x55, 0xAA.toByte(), 0x02, 0x10, 0x20), frames[0])
        assertContentEquals(byteArrayOf(0x55, 0xAA.toByte(), 0x01, 0x7F), frames[1])
    }
}
