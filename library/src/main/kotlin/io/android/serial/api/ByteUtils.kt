package io.android.serial.api

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Converts bytes to an uppercase hexadecimal string. */
public fun ByteArray.toHex(separator: String = ""): String = joinToString(separator) { "%02X".format(it.toInt() and 0xFF) }

/** Parses a hexadecimal string. Whitespace is ignored. */
public fun String.hexToByteArray(): ByteArray {
    val value = filterNot(Char::isWhitespace)
    require(value.length % 2 == 0) { "Hex string must contain an even number of digits" }
    return ByteArray(value.length / 2) { index ->
        val high = value[index * 2].digitToIntOrNull(16)
        val low = value[index * 2 + 1].digitToIntOrNull(16)
        require(high != null && low != null) { "Invalid hexadecimal value: $this" }
        ((high shl 4) or low).toByte()
    }
}

public fun ByteArray.toShort(offset: Int = 0, order: ByteOrder = ByteOrder.BIG_ENDIAN): Short = buffer(offset, Short.SIZE_BYTES, order).short
public fun ByteArray.toInt(offset: Int = 0, order: ByteOrder = ByteOrder.BIG_ENDIAN): Int = buffer(offset, Int.SIZE_BYTES, order).int
public fun ByteArray.toFloat(offset: Int = 0, order: ByteOrder = ByteOrder.BIG_ENDIAN): Float = buffer(offset, Float.SIZE_BYTES, order).float
public fun ByteArray.toDouble(offset: Int = 0, order: ByteOrder = ByteOrder.BIG_ENDIAN): Double = buffer(offset, Double.SIZE_BYTES, order).double

public fun Short.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray = ByteBuffer.allocate(Short.SIZE_BYTES).order(order).putShort(this).array()
public fun Int.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray = ByteBuffer.allocate(Int.SIZE_BYTES).order(order).putInt(this).array()
public fun Float.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray = ByteBuffer.allocate(Float.SIZE_BYTES).order(order).putFloat(this).array()
public fun Double.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray = ByteBuffer.allocate(Double.SIZE_BYTES).order(order).putDouble(this).array()

/** Returns one Boolean per bit, in wire order for the selected byte order. */
public fun ByteArray.toBooleanArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): BooleanArray {
    val result = BooleanArray(size * Byte.SIZE_BITS)
    forEachIndexed { byteIndex, value ->
        val byte = value.toInt() and 0xFF
        repeat(Byte.SIZE_BITS) { bitIndex ->
            val shift = if (order == ByteOrder.BIG_ENDIAN) 7 - bitIndex else bitIndex
            result[byteIndex * Byte.SIZE_BITS + bitIndex] = (byte and (1 shl shift)) != 0
        }
    }
    return result
}

/** Packs bits in wire order into bytes. The bit count must be byte-aligned. */
public fun BooleanArray.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
    require(size % Byte.SIZE_BITS == 0) { "Boolean array length must be a multiple of 8" }
    return ByteArray(size / Byte.SIZE_BITS) { byteIndex ->
        var value = 0
        repeat(Byte.SIZE_BITS) { bitIndex ->
            if (this[byteIndex * Byte.SIZE_BITS + bitIndex]) {
                val shift = if (order == ByteOrder.BIG_ENDIAN) 7 - bitIndex else bitIndex
                value = value or (1 shl shift)
            }
        }
        value.toByte()
    }
}

public fun ByteArray.concat(vararg arrays: ByteArray): ByteArray = this + arrays.fold(ByteArray(0)) { result, array -> result + array }

private fun ByteArray.buffer(offset: Int, length: Int, order: ByteOrder): ByteBuffer {
    require(offset >= 0 && offset <= size - length) { "Range [$offset, ${offset + length}) is outside byte array" }
    return ByteBuffer.wrap(this, offset, length).order(order)
}
