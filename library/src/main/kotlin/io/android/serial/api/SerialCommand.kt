package io.android.serial.api

import java.time.Duration
import java.util.UUID
import kotlinx.coroutines.Deferred

data class SerialCommand(
    val payload: ByteArray,
    val timeout: Duration? = null,
    val delay: Duration? = null,
    val maxRetries: Int = 0,
    val retryDelay: Duration = Duration.ZERO,
    val tag: String? = null,
    val id: String = UUID.randomUUID().toString()
) {
    init {
        require(payload.isNotEmpty()) { "Serial payload must not be empty" }
        require(maxRetries >= 0) { "Max retries cannot be negative" }
        require(!retryDelay.isNegative) { "Retry delay cannot be negative" }
    }
}

data class CommandHandle(val id: String, val completion: Deferred<CommandResult>)

sealed interface CommandResult {
    data class Sent(val bytes: Int) : CommandResult
    data class TimedOut(val timeout: Duration) : CommandResult
    data class Failed(val error: SerialError) : CommandResult
    data object Cancelled : CommandResult
}

