package com.jaidensiu.orbit.queue

import com.jaidensiu.orbit.OrbitConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

internal class EventQueue(
    private val storage: QueueStorage,
    private val config: OrbitConfig,
    private val random: Random = Random.Default,
) {
    private val mutex = Mutex()

    suspend fun enqueue(envelope: EventEnvelope, enqueuedAtMillis: Long) {
        val id = "$enqueuedAtMillis-${random.nextLong().toString(radix = 36)}"
        mutex.withLock {
            storage.append(QueuedEvent(id = id, enqueuedAtMillis = enqueuedAtMillis, envelope = envelope))
        }
    }

    suspend fun peekBatch(): List<QueuedEvent> = mutex.withLock {
        storage.readAll().take(n = config.batchSize)
    }

    suspend fun ack(ids: Collection<String>) {
        if (ids.isEmpty()) return
        mutex.withLock { storage.removeIds(ids.toSet()) }
    }

    suspend fun evictExpired(nowMillis: Long): Int = mutex.withLock {
        val all = storage.readAll()
        val maxAgeMillis = config.maxEventAge.inWholeMilliseconds
        val notExpired = all.filter { nowMillis - it.enqueuedAtMillis <= maxAgeMillis }
        val bounded = if (notExpired.size > config.maxQueueSize) {
            notExpired.takeLast(n = config.maxQueueSize)
        } else {
            notExpired
        }
        val evictedCount = all.size - bounded.size
        if (evictedCount > 0) {
            storage.replaceAll(events = bounded)
        }
        evictedCount
    }
}
