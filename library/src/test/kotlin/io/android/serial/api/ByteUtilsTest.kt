package io.android.serial.api

import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ByteUtilsTest {
    @Test
    fun convertsAllNumericTypesInBothByteOrders() {
        for (order in arrayOf(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)) {
            val short = (-12345).toShort()
            val int = 0x12345678
            val float = -12.5f
            val double = 12345.6789
            assertEquals(short, short.toByteArray(order).toShort(order = order))
            assertEquals(int, int.toByteArray(order).toInt(order = order))
            assertEquals(float.toBits(), float.toByteArray(order).toFloat(order = order).toBits())
            assertEquals(double.toBits(), double.toByteArray(order).toDouble(order = order).toBits())
        }
    }

    @Test
    fun convertsHexAndConcatenates() {
        assertEquals("00 FF 10", byteArrayOf(0, -1, 0x10).toHex(" "))
        assertContentEquals(byteArrayOf(0, -1, 0x10), "00 ff\n10".hexToByteArray())
        assertContentEquals(byteArrayOf(1, 2, 3), byteArrayOf(1).concat(byteArrayOf(2), byteArrayOf(3)))
    }

    @Test
    fun convertsBitsInBothOrders() {
        val bytes = byteArrayOf(0x81.toByte(), 0x24)
        for (order in arrayOf(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)) {
            assertContentEquals(bytes, bytes.toBooleanArray(order).toByteArray(order))
        }
        assertEquals(
            listOf(true, false, false, false, false, false, false, true),
            byteArrayOf(0x81.toByte()).toBooleanArray(ByteOrder.BIG_ENDIAN).toList()
        )
        assertEquals(
            listOf(true, false, false, false, false, false, false, true),
            byteArrayOf(0x81.toByte()).toBooleanArray(ByteOrder.LITTLE_ENDIAN).toList()
        )
    }

    @Test
    fun rejectsInvalidInput() {
        assertFailsWith<IllegalArgumentException> { "ABC".hexToByteArray() }
        assertFailsWith<IllegalArgumentException> { "GG".hexToByteArray() }
        assertFailsWith<IllegalArgumentException> { byteArrayOf(1).toInt() }
        assertFailsWith<IllegalArgumentException> { BooleanArray(7).toByteArray() }
    }
}
