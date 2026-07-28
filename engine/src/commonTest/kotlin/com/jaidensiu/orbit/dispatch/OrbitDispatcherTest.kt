package com.jaidensiu.orbit.dispatch

import com.jaidensiu.orbit.DeliveryFailure
import com.jaidensiu.orbit.OrbitConfig
import com.jaidensiu.orbit.destination.DeliveryOutcome
import com.jaidensiu.orbit.queue.EventQueue
import com.jaidensiu.orbit.testing.FakeClock
import com.jaidensiu.orbit.testing.FakeDestination
import com.jaidensiu.orbit.testing.InMemoryQueueStorage
import com.jaidensiu.orbit.testing.testEnvelope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OrbitDispatcherTest {

    private fun config(): OrbitConfig {
        return OrbitConfig(
            baseUrl = "https://example.com",
            batchSize = 10,
            flushInterval = 50.milliseconds,
            callTimeout = 1.seconds,
        )
    }

    @Test
    fun successfulDeliveryAcksTheBatchAndReportsNoFailure() {
        runTest {
            val storage = InMemoryQueueStorage()
            val queue = EventQueue(storage = storage, config = config())
            queue.enqueue(envelope = testEnvelope(name = "a"), enqueuedAtMillis = 0)

            val destination = FakeDestination { batch -> batch.associate { it.id to DeliveryOutcome.Success } }
            val failures = mutableListOf<DeliveryFailure>()
            val dispatcher = OrbitDispatcher(
                destination = destination,
                queue = queue,
                config = config(),
                retryPolicy = RetryPolicy(),
                circuitBreaker = CircuitBreaker(clock = FakeClock()),
                clock = FakeClock(),
                onDeliveryFailure = { failures += it },
            )

            val job = dispatcher.start(scope = this)
            runCurrent()

            assertTrue(actual = storage.readAll().isEmpty())
            assertTrue(actual = failures.isEmpty())

            job.cancel()
        }
    }

    @Test
    fun retryableFailureKeepsTheEventQueuedAndRetriesAfterBackoff() {
        runTest {
            val storage = InMemoryQueueStorage()
            val cfg = config()
            val queue = EventQueue(storage = storage, config = cfg)
            queue.enqueue(envelope = testEnvelope(name = "a"), enqueuedAtMillis = 0)

            var attempts = 0
            val destination = FakeDestination { batch ->
                attempts++
                batch.associate { it.id to DeliveryOutcome.RetryableFailure(reason = "boom") }
            }
            val dispatcher = OrbitDispatcher(
                destination = destination,
                queue = queue,
                config = cfg,
                retryPolicy = RetryPolicy(config = RetryConfig(baseDelay = 10.milliseconds, maxDelay = 100.milliseconds)),
                circuitBreaker = CircuitBreaker(config = CircuitBreakerConfig(failureThreshold = 100), clock = FakeClock()),
                clock = FakeClock(),
                onDeliveryFailure = {},
            )

            val job = dispatcher.start(scope = this)
            runCurrent()
            assertEquals(expected = 1, actual = attempts)
            assertEquals(expected = 1, actual = storage.readAll().size)

            advanceTimeBy(delayTimeMillis = 200.milliseconds.inWholeMilliseconds)
            runCurrent()
            assertTrue(actual = attempts > 1, message = "expected a retry after backoff, only saw $attempts attempt(s)")

            job.cancel()
        }
    }

    @Test
    fun permanentFailureDropsTheEventAndReportsIt() {
        runTest {
            val storage = InMemoryQueueStorage()
            val cfg = config()
            val queue = EventQueue(storage = storage, config = cfg)
            queue.enqueue(envelope = testEnvelope(name = "a"), enqueuedAtMillis = 0)

            val destination = FakeDestination { batch ->
                batch.associate { it.id to DeliveryOutcome.PermanentFailure(reason = "bad request") }
            }
            val failures = mutableListOf<DeliveryFailure>()
            val dispatcher = OrbitDispatcher(
                destination = destination,
                queue = queue,
                config = cfg,
                retryPolicy = RetryPolicy(),
                circuitBreaker = CircuitBreaker(clock = FakeClock()),
                clock = FakeClock(),
                onDeliveryFailure = { failures += it },
            )

            val job = dispatcher.start(scope = this)
            runCurrent()

            assertTrue(actual = storage.readAll().isEmpty())
            assertEquals(expected = 1, actual = failures.size)
            assertTrue(actual = failures.single().dropped)

            job.cancel()
        }
    }

    @Test
    fun repeatedFailuresOpenTheCircuitBreakerAndPauseAttempts() {
        runTest {
            val storage = InMemoryQueueStorage()
            val cfg = config()
            val queue = EventQueue(storage = storage, config = cfg)
            queue.enqueue(envelope = testEnvelope(name = "a"), enqueuedAtMillis = 0)

            var attempts = 0
            val destination = FakeDestination { batch ->
                attempts++
                batch.associate { it.id to DeliveryOutcome.RetryableFailure(reason = "boom") }
            }
            val dispatcher = OrbitDispatcher(
                destination = destination,
                queue = queue,
                config = cfg,
                retryPolicy = RetryPolicy(config = RetryConfig(baseDelay = 1.milliseconds, maxDelay = 5.milliseconds)),
                circuitBreaker = CircuitBreaker(
                    config = CircuitBreakerConfig(failureThreshold = 2, openDuration = 1.seconds),
                    clock = FakeClock(),
                ),
                clock = FakeClock(),
                onDeliveryFailure = {},
            )

            val job = dispatcher.start(scope = this)
            advanceTimeBy(delayTimeMillis = 50.milliseconds.inWholeMilliseconds)
            runCurrent()
            val attemptsWhileClosed = attempts

            advanceTimeBy(delayTimeMillis = 500.milliseconds.inWholeMilliseconds)
            runCurrent()

            assertEquals(
                expected = attemptsWhileClosed,
                actual = attempts,
                message = "circuit breaker should have stopped further attempts once open",
            )

            job.cancel()
        }
    }
}
