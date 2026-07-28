package com.jaidensiu.orbit.testing

import com.jaidensiu.orbit.destination.DeliveryOutcome
import com.jaidensiu.orbit.destination.Destination
import com.jaidensiu.orbit.queue.EventEnvelope
import com.jaidensiu.orbit.queue.QueueStorage
import com.jaidensiu.orbit.queue.QueuedEvent
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

internal class FakeClock(private var instant: Instant = Instant.fromEpochMilliseconds(epochMilliseconds = 0)) : Clock {
    override fun now(): Instant {
        return instant
    }

    fun advanceBy(duration: Duration) {
        instant += duration
    }
}

internal class InMemoryQueueStorage : QueueStorage {
    private val events = mutableListOf<QueuedEvent>()

    override suspend fun append(event: QueuedEvent) {
        events += event
    }

    override suspend fun readAll(): List<QueuedEvent> {
        return events.toList()
    }

    override suspend fun removeIds(ids: Set<String>) {
        events.removeAll { it.id in ids }
    }

    override suspend fun replaceAll(events: List<QueuedEvent>) {
        this.events.clear()
        this.events += events
    }
}

internal class FakeDestination(
    override val id: String = "fake",
    private val respond: (List<QueuedEvent>) -> Map<String, DeliveryOutcome>,
) : Destination {
    override suspend fun send(batch: List<QueuedEvent>): Map<String, DeliveryOutcome> {
        return respond(batch)
    }
}

internal fun testEnvelope(name: String, timestampMillis: Long = 0): EventEnvelope {
    return EventEnvelope(eventName = name, timestampMillis = timestampMillis, properties = emptyMap())
}

internal fun testQueuedEvent(id: String, name: String, enqueuedAtMillis: Long = 0): QueuedEvent {
    return QueuedEvent(
        id = id,
        enqueuedAtMillis = enqueuedAtMillis,
        envelope = testEnvelope(name = name),
    )
}
