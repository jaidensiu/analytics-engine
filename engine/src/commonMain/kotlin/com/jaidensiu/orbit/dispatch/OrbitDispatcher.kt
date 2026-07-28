package com.jaidensiu.orbit.dispatch

import com.jaidensiu.orbit.DeliveryFailure
import com.jaidensiu.orbit.OrbitConfig
import com.jaidensiu.orbit.destination.DeliveryOutcome
import com.jaidensiu.orbit.destination.Destination
import com.jaidensiu.orbit.queue.EventQueue
import com.jaidensiu.orbit.queue.QueuedEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock

internal class OrbitDispatcher(
    private val destination: Destination,
    private val queue: EventQueue,
    private val config: OrbitConfig,
    private val retryPolicy: RetryPolicy,
    private val circuitBreaker: CircuitBreaker,
    private val clock: Clock,
    private val onDeliveryFailure: (DeliveryFailure) -> Unit,
) {
    private var consecutiveFailures = 0

    fun start(scope: CoroutineScope): Job {
        return scope.launch {
            while (isActive) {
                try {
                    runIteration()
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    onDeliveryFailure(
                        DeliveryFailure(
                            destinationId = destination.id,
                            reason = "queue storage error: ${t.message}",
                            cause = t,
                            timestampMillis = clock.now().toEpochMilliseconds(),
                            dropped = false,
                        ),
                    )
                    delay(duration = config.flushInterval)
                }
            }
        }
    }

    private suspend fun runIteration() {
        queue.evictExpired(nowMillis = clock.now().toEpochMilliseconds())
        if (!circuitBreaker.canAttempt()) {
            delay(duration = config.flushInterval)
            return
        }
        val batch = queue.peekBatch()
        if (batch.isEmpty()) {
            delay(duration = config.flushInterval)
            return
        }
        val outcomes = withTimeoutOrNull(timeout = config.callTimeout * batch.size) {
            destination.send(batch = batch)
        }
        if (outcomes == null) {
            onDeliveryFailure(
                DeliveryFailure(
                    destinationId = destination.id,
                    reason = "timed out sending a batch of ${batch.size}",
                    cause = null,
                    timestampMillis = clock.now().toEpochMilliseconds(),
                    dropped = false,
                ),
            )
            onFailure()
            return
        }
        val ackIds = mutableListOf<String>()
        var anyRetryable = false
        for (event in batch) {
            when (val outcome = outcomes[event.id] ?: MISSING_OUTCOME) {
                DeliveryOutcome.Success -> ackIds += event.id
                is DeliveryOutcome.PermanentFailure -> {
                    ackIds += event.id
                    onDeliveryFailure(event.toDeliveryFailure(outcome = outcome, dropped = true))
                }
                is DeliveryOutcome.RetryableFailure -> {
                    anyRetryable = true
                    onDeliveryFailure(event.toDeliveryFailure(outcome = outcome, dropped = false))
                }
            }
        }
        queue.ack(ids = ackIds)
        if (anyRetryable) {
            onFailure()
        } else {
            circuitBreaker.recordSuccess()
            consecutiveFailures = 0
            delay(duration = config.flushInterval)
        }
    }

    private suspend fun onFailure() {
        circuitBreaker.recordFailure()
        delay(duration = retryPolicy.delayFor(attempt = consecutiveFailures++))
    }

    private fun QueuedEvent.toDeliveryFailure(outcome: DeliveryOutcome, dropped: Boolean): DeliveryFailure {
        return DeliveryFailure(
            destinationId = destination.id,
            reason = (outcome as? DeliveryOutcome.PermanentFailure)?.reason
                ?: (outcome as? DeliveryOutcome.RetryableFailure)?.reason
                ?: "unknown",
            cause = (outcome as? DeliveryOutcome.PermanentFailure)?.cause
                ?: (outcome as? DeliveryOutcome.RetryableFailure)?.cause,
            timestampMillis = clock.now().toEpochMilliseconds(),
            dropped = dropped,
        )
    }

    private companion object {
        val MISSING_OUTCOME = DeliveryOutcome.RetryableFailure(reason = "no outcome returned by destination")
    }
}
