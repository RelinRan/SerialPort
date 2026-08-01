package io.android.serial.api

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ByteUtilsTest {
    @Test
    fun convertsHexWithWhitespaceAndSeparators() {
        assertEquals("00 FF 10", ByteUtils.toHex(byteArrayOf(0, -1, 0x10), " "))
        assertContentEquals(byteArrayOf(0, -1, 0x10), ByteUtils.fromHex("00 ff\n10"))
    }

    @Test
    fun readsBothCommonByteOrders() {
        val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        assertEquals(0x0201, ByteUtils.readUInt16LE(bytes))
        assertEquals(0x0102, ByteUtils.readUInt16BE(bytes))
        assertEquals(0x04030201, ByteUtils.readInt32LE(bytes))
        assertEquals(0x01020304, ByteUtils.readInt32BE(bytes))
    }

    @Test
    fun rejectsInvalidHexAndRanges() {
        assertFailsWith<IllegalArgumentException> { ByteUtils.fromHex("ABC") }
        assertFailsWith<IllegalArgumentException> { ByteUtils.fromHex("GG") }
        assertFailsWith<IllegalArgumentException> { ByteUtils.readUInt16LE(byteArrayOf(1), 0) }
    }
}
