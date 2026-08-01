package io.github.relinran.serialport.core

import java.time.Instant

sealed interface SerialEvent {
    data class StateChanged(val state: SerialState) : SerialEvent
    data class DataReceived(val data: ByteArray, val receivedAt: Instant = Instant.now()) : SerialEvent
    data class CommandSent(val id: String, val bytes: Int, val sentAt: Instant = Instant.now()) : SerialEvent
    data class CommandTimedOut(val id: String, val tag: String?, val timeoutMillis: Long) : SerialEvent
    data class ErrorRaised(val error: SerialError) : SerialEvent
}
