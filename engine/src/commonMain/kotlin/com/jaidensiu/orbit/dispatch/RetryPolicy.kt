package com.jaidensiu.orbit.dispatch

import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class RetryConfig(
    val baseDelay: Duration = 1.seconds,
    val maxDelay: Duration = 5.minutes,
)

internal class RetryPolicy(
    private val config: RetryConfig = RetryConfig(),
    private val random: Random = Random.Default,
) {
    fun delayFor(attempt: Int): Duration {
        require(value = attempt >= 0) { "attempt must be >= 0, was $attempt" }
        val cappedAttempt = attempt.coerceAtMost(maximumValue = MAX_EXPONENT)
        val ceiling = (config.baseDelay * 2.0.pow(n = cappedAttempt)).coerceAtMost(maximumValue = config.maxDelay)
        return ceiling * random.nextDouble()
    }

    private companion object {
        const val MAX_EXPONENT = 30
    }
}
