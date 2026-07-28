@file:OptIn(ExperimentalAtomicApi::class)

package com.jaidensiu.orbit

import com.jaidensiu.orbit.catalog.AnalyticsEvent
import com.jaidensiu.orbit.destination.Destination
import com.jaidensiu.orbit.dispatch.CircuitBreaker
import com.jaidensiu.orbit.dispatch.OrbitDispatcher
import com.jaidensiu.orbit.dispatch.RetryPolicy
import com.jaidensiu.orbit.queue.EventQueue
import com.jaidensiu.orbit.queue.FileQueueStorage
import com.jaidensiu.orbit.queue.toEnvelope
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock

class Orbit internal constructor(
    private val config: OrbitConfig,
    destinations: List<Destination>,
    queueDirectory: String,
    private val httpClient: HttpClient,
    private val clock: Clock = Clock.System,
    private val scope: CoroutineScope = CoroutineScope(context = SupervisorJob() + Dispatchers.Default),
) {
    private val listeners = AtomicReference<List<(DeliveryFailure) -> Unit>>(emptyList())

    private val destinationQueues: List<Pair<Destination, EventQueue>> = destinations.map { destination ->
        destination to EventQueue(
            storage = FileQueueStorage(Path(base = queueDirectory, parts = arrayOf("${destination.id}.jsonl"))),
            config = config,
        )
    }

    init {
        destinationQueues.forEach { (destination, queue) ->
            OrbitDispatcher(
                destination = destination,
                queue = queue,
                config = config,
                retryPolicy = RetryPolicy(config = config.retryConfig),
                circuitBreaker = CircuitBreaker(config = config.circuitBreakerConfig, clock = clock),
                clock = clock,
                onDeliveryFailure = ::notifyListeners,
            ).start(scope)
        }
    }

    fun track(event: AnalyticsEvent) {
        val envelope = event.toEnvelope(config = config, timestampMillis = clock.now().toEpochMilliseconds())
        scope.launch {
            for ((destination, queue) in destinationQueues) {
                try {
                    queue.enqueue(envelope = envelope, enqueuedAtMillis = envelope.timestampMillis)
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    notifyListeners(
                        DeliveryFailure(
                            destinationId = destination.id,
                            reason = "failed to persist event: ${t.message}",
                            cause = t,
                            timestampMillis = clock.now().toEpochMilliseconds(),
                            dropped = true,
                        ),
                    )
                }
            }
        }
    }

    fun addDeliveryFailureListener(listener: (DeliveryFailure) -> Unit) {
        while (true) {
            val current = listeners.load()
            val updated = current + listener
            if (listeners.compareAndSet(expectedValue = current, newValue = updated)) {
                return
            }
        }
    }

    private fun notifyListeners(failure: DeliveryFailure) {
        scope.launch {
            for (listener in listeners.load()) {
                try {
                    listener(failure)
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    println("[warn] Orbit: a deliveryFailureListener threw: $t")
                }
            }
        }
    }

    fun shutdown() {
        val dispatchJob = scope.coroutineContext.job
        dispatchJob.cancel()
        CoroutineScope(context = Dispatchers.Default).launch {
            try {
                dispatchJob.join()
                httpClient.close()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                println("[warn] Orbit: shutdown() failed: $t")
            }
        }
    }
}
