package android.serial.port.api

sealed interface SerialState {
    data object Idle : SerialState
    data object Connecting : SerialState
    data class Connected(
        val config: SerialConfig,
        val queuedCount: Int = 0,
        val bytesSent: Long = 0,
        val bytesReceived: Long = 0
    ) : SerialState
    data class Closing(val reason: CloseReason) : SerialState
    data class Failed(val error: SerialError, val recoverable: Boolean) : SerialState
    data object Closed : SerialState
}

enum class CloseReason { Requested, Failed }

