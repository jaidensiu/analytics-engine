package com.jaidensiu.orbit.dispatch

import com.jaidensiu.orbit.testing.FakeClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CircuitBreakerTest {

    @Test
    fun startsClosedAndAllowsAttempts() {
        runTest {
            val breaker = CircuitBreaker(clock = FakeClock())

            assertTrue(actual = breaker.canAttempt())
        }
    }

    @Test
    fun opensAfterConsecutiveFailuresReachThreshold() {
        runTest {
            val breaker = CircuitBreaker(config = CircuitBreakerConfig(failureThreshold = 3), clock = FakeClock())

            repeat(times = 3) { breaker.recordFailure() }

            assertFalse(actual = breaker.canAttempt())
        }
    }

    @Test
    fun aSuccessBeforeReachingTheThresholdResetsTheFailureCount() {
        runTest {
            val breaker = CircuitBreaker(config = CircuitBreakerConfig(failureThreshold = 3), clock = FakeClock())

            repeat(times = 2) { breaker.recordFailure() }
            breaker.recordSuccess()
            repeat(times = 2) { breaker.recordFailure() }

            assertTrue(actual = breaker.canAttempt())
        }
    }

    @Test
    fun reopensToHalfOpenOnceCooldownElapsesAndAllowsExactlyOneTrial() {
        runTest {
            val clock = FakeClock()
            val breaker = CircuitBreaker(
                config = CircuitBreakerConfig(failureThreshold = 1, openDuration = 10.seconds),
                clock = clock,
            )

            breaker.recordFailure()
            assertFalse(actual = breaker.canAttempt())

            clock.advanceBy(duration = 10.seconds)
            assertTrue(actual = breaker.canAttempt())
            assertFalse(actual = breaker.canAttempt())
        }
    }

    @Test
    fun aSuccessfulHalfOpenTrialClosesTheBreaker() {
        runTest {
            val clock = FakeClock()
            val breaker = CircuitBreaker(
                config = CircuitBreakerConfig(failureThreshold = 1, openDuration = 10.seconds),
                clock = clock,
            )

            breaker.recordFailure()
            clock.advanceBy(duration = 10.seconds)
            breaker.canAttempt()
            breaker.recordSuccess()

            assertTrue(actual = breaker.canAttempt())
        }
    }

    @Test
    fun aFailedHalfOpenTrialDoublesTheNextCooldown() {
        runTest {
            val clock = FakeClock()
            val breaker = CircuitBreaker(
                config = CircuitBreakerConfig(failureThreshold = 1, openDuration = 10.seconds, maxOpenDuration = 1.minutes),
                clock = clock,
            )

            breaker.recordFailure()
            clock.advanceBy(duration = 10.seconds)
            breaker.canAttempt()
            breaker.recordFailure()

            clock.advanceBy(duration = 19.seconds)
            assertFalse(actual = breaker.canAttempt())

            clock.advanceBy(duration = 1.seconds)
            assertTrue(actual = breaker.canAttempt())
        }
    }
}
