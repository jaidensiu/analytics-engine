package com.jaidensiu.orbit.dispatch

import kotlin.math.pow
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RetryPolicyTest {

    @Test
    fun delayIsWithinFullJitterBoundsForEachAttempt() {
        val config = RetryConfig(baseDelay = 1.seconds, maxDelay = 1.minutes)
        val policy = RetryPolicy(config = config, random = Random(seed = 42))

        repeat(times = 20) { attempt ->
            val delay = policy.delayFor(attempt = attempt)
            val ceiling = minOf(a = config.baseDelay * 2.0.pow(n = attempt), b = config.maxDelay)

            assertTrue(actual = delay >= Duration.ZERO, message = "delay $delay for attempt $attempt should be >= 0")
            assertTrue(actual = delay <= ceiling, message = "delay $delay for attempt $attempt should be <= ceiling $ceiling")
        }
    }

    @Test
    fun delayNeverExceedsMaxDelayEvenForHugeAttempts() {
        val config = RetryConfig(baseDelay = 1.seconds, maxDelay = 30.seconds)
        val policy = RetryPolicy(config = config, random = Random(seed = 7))

        val delay = policy.delayFor(attempt = 1000)

        assertTrue(actual = delay <= config.maxDelay)
    }

    @Test
    fun negativeAttemptIsRejected() {
        val policy = RetryPolicy()

        assertFailsWith<IllegalArgumentException> { policy.delayFor(attempt = -1) }
    }
}
