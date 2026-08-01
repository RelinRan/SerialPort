package io.github.relinran.serialport.core

sealed interface SerialError {
    data object DeviceNotFound : SerialError
    data object PermissionDenied : SerialError
    data object UnsupportedAbi : SerialError
    data class OpenFailed(val cause: Throwable? = null) : SerialError
    data class ReadFailed(val cause: Throwable? = null) : SerialError
    data class WriteFailed(val cause: Throwable? = null) : SerialError
    data object AlreadyClosed : SerialError
    data object NotConnected : SerialError
    data object QueueFull : SerialError
    data object Cancelled : SerialError
}
