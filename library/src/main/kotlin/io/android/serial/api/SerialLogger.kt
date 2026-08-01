package io.android.serial.api

import java.time.Instant

public enum class SerialDirection { Tx, Rx }

public fun interface SerialLogger {
    public fun log(direction: SerialDirection, data: ByteArray, timestamp: Instant)
}

public object NoOpSerialLogger : SerialLogger {
    override fun log(direction: SerialDirection, data: ByteArray, timestamp: Instant) = Unit
}
