package android.serial.port.api

/** Common byte-array conversions used by serial protocols. */
public object ByteUtils {
    private val HEX = "0123456789ABCDEF".toCharArray()

    @JvmStatic
    public fun toHex(bytes: ByteArray, separator: String = ""): String = buildString(bytes.size * (2 + separator.length)) {
        bytes.forEachIndexed { index, byte ->
            if (index > 0) append(separator)
            val value = byte.toInt() and 0xFF
            append(HEX[value ushr 4])
            append(HEX[value and 0x0F])
        }
    }

    @JvmStatic
    public fun fromHex(value: String): ByteArray {
        val normalized = value.filterNot(Char::isWhitespace)
        require(normalized.length % 2 == 0) { "Hex string must contain an even number of digits" }
        return ByteArray(normalized.length / 2) { index ->
            val high = Character.digit(normalized[index * 2], 16)
            val low = Character.digit(normalized[index * 2 + 1], 16)
            require(high >= 0 && low >= 0) { "Invalid hexadecimal value: $value" }
            ((high shl 4) or low).toByte()
        }
    }

    @JvmStatic
    public fun readUInt16LE(bytes: ByteArray, offset: Int = 0): Int {
        checkRange(bytes, offset, 2)
        return (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
    }

    @JvmStatic
    public fun readUInt16BE(bytes: ByteArray, offset: Int = 0): Int {
        checkRange(bytes, offset, 2)
        return ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
    }

    @JvmStatic
    public fun readInt32LE(bytes: ByteArray, offset: Int = 0): Int {
        checkRange(bytes, offset, 4)
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            (bytes[offset + 3].toInt() shl 24)
    }

    @JvmStatic
    public fun readInt32BE(bytes: ByteArray, offset: Int = 0): Int {
        checkRange(bytes, offset, 4)
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
    }

    @JvmStatic
    public fun concat(vararg arrays: ByteArray): ByteArray = arrays.fold(ByteArray(0)) { result, array -> result + array }

    private fun checkRange(bytes: ByteArray, offset: Int, length: Int) {
        require(offset >= 0 && offset <= bytes.size - length) { "Range [$offset, ${offset + length}) is outside byte array" }
    }
}
