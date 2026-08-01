package io.android.serial.api

import java.time.Duration

data class SerialConfig(
    val path: String,
    val baudRate: Int,
    val mode: SerialMode = SerialMode.ReadWrite,
    val readBufferSize: Int = 512,
    val queue: QueueConfig = QueueConfig(),
    val reconnect: ReconnectPolicy = ReconnectPolicy()
) {
    init {
        require(path.isNotBlank()) { "Serial path must not be blank" }
        require(baudRate > 0) { "Baud rate must be positive" }
        require(readBufferSize > 0) { "Read buffer size must be positive" }
    }
}

enum class SerialMode { ReadOnly, WriteOnly, ReadWrite }

data class QueueConfig(
    val capacity: Int = 256,
    val overflow: QueueOverflow = QueueOverflow.Reject,
    val defaultWriteDelay: Duration = Duration.ZERO,
    val defaultTimeout: Duration = Duration.ofMillis(500)
) {
    init {
        require(capacity > 0) { "Queue capacity must be positive" }
        require(!defaultWriteDelay.isNegative) { "Write delay cannot be negative" }
        require(!defaultTimeout.isNegative) { "Timeout cannot be negative" }
    }
}

enum class QueueOverflow { Reject, DropOldest }

