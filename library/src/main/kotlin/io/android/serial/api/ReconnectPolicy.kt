package io.android.serial.api

import java.time.Duration

public data class ReconnectPolicy(
    val enabled: Boolean = false,
    val maxAttempts: Int = 3,
    val initialDelay: Duration = Duration.ofMillis(500),
    val maxDelay: Duration = Duration.ofSeconds(10),
    val multiplier: Double = 2.0
) {
    init {
        require(maxAttempts >= 0) { "Max reconnect attempts cannot be negative" }
        require(!initialDelay.isNegative && !maxDelay.isNegative) { "Reconnect delays cannot be negative" }
        require(maxDelay >= initialDelay) { "Maximum reconnect delay must not be less than initial delay" }
        require(multiplier >= 1.0) { "Reconnect multiplier must be at least 1" }
    }

    public fun delayFor(attempt: Int): Duration {
        require(attempt >= 0)
        val millis = (initialDelay.toMillis() * Math.pow(multiplier, attempt.toDouble())).toLong()
        return Duration.ofMillis(millis.coerceAtMost(maxDelay.toMillis()))
    }
}
