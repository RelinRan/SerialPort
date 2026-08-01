package io.android.serial.api

/** Pluggable checksum calculation for device protocols. */
public fun interface Checksum {
    public fun calculate(data: ByteArray): ByteArray
}

public object Checksums {
    public val None: Checksum = Checksum { ByteArray(0) }

    public val Xor8: Checksum = Checksum { data ->
        byteArrayOf(data.fold(0) { result, byte -> result xor (byte.toInt() and 0xFF) }.toByte())
    }

    public val Sum8: Checksum = Checksum { data ->
        byteArrayOf(data.fold(0) { result, byte -> (result + (byte.toInt() and 0xFF)) and 0xFF }.toByte())
    }

    public val Crc16Modbus: Checksum = Checksum { data ->
        var crc = 0xFFFF
        data.forEach { byte ->
            crc = crc xor (byte.toInt() and 0xFF)
            repeat(8) { crc = if ((crc and 1) != 0) (crc ushr 1) xor 0xA001 else crc ushr 1 }
        }
        byteArrayOf((crc and 0xFF).toByte(), (crc ushr 8).toByte())
    }
}

public fun ByteArray.withChecksum(checksum: Checksum): ByteArray = this + checksum.calculate(this)

public fun ByteArray.hasChecksum(checksum: Checksum): Boolean {
    if (size < checksum.calculate(ByteArray(0)).size) return false
    val bodySize = size - checksum.calculate(ByteArray(0)).size
    return copyOf(bodySize).let { body -> body.withChecksum(checksum).contentEquals(this) }
}
