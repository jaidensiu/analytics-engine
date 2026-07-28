package com.jaidensiu.orbit.queue

import com.jaidensiu.orbit.OrbitConfig
import com.jaidensiu.orbit.testing.InMemoryQueueStorage
import com.jaidensiu.orbit.testing.testEnvelope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class EventQueueTest {

    @Test
    fun peekBatchReturnsEventsInFifoOrderUpToBatchSize() {
        runTest {
            val storage = InMemoryQueueStorage()
            val queue = EventQueue(storage = storage, config = OrbitConfig(baseUrl = "https://example.com", batchSize = 2))

            queue.enqueue(envelope = testEnvelope(name = "a"), enqueuedAtMillis = 0)
            queue.enqueue(envelope = testEnvelope(name = "b"), enqueuedAtMillis = 1)
            queue.enqueue(envelope = testEnvelope(name = "c"), enqueuedAtMillis = 2)

            val batch = queue.peekBatch()

            assertEquals(expected = listOf("a", "b"), actual = batch.map { it.envelope.eventName })
        }
    }

    @Test
    fun ackRemovesOnlyTheGivenIds() {
        runTest {
            val storage = InMemoryQueueStorage()
            val queue = EventQueue(storage = storage, config = OrbitConfig(baseUrl = "https://example.com"))

            queue.enqueue(envelope = testEnvelope(name = "a"), enqueuedAtMillis = 0)
            queue.enqueue(envelope = testEnvelope(name = "b"), enqueuedAtMillis = 1)
            val batch = queue.peekBatch()

            queue.ack(ids = listOf(batch.first().id))

            assertEquals(expected = listOf("b"), actual = storage.readAll().map { it.envelope.eventName })
        }
    }

    @Test
    fun evictExpiredDropsEventsOlderThanMaxAge() {
        runTest {
            val storage = InMemoryQueueStorage()
            val queue = EventQueue(
                storage = storage,
                config = OrbitConfig(baseUrl = "https://example.com", maxEventAge = 1.hours),
            )

            queue.enqueue(envelope = testEnvelope(name = "old"), enqueuedAtMillis = 0)
            queue.enqueue(envelope = testEnvelope(name = "new"), enqueuedAtMillis = 2.hours.inWholeMilliseconds)

            val evicted = queue.evictExpired(nowMillis = 2.hours.inWholeMilliseconds)

            assertEquals(expected = 1, actual = evicted)
            assertEquals(expected = listOf("new"), actual = storage.readAll().map { it.envelope.eventName })
        }
    }

    @Test
    fun evictExpiredTrimsToMaxQueueSizeKeepingTheNewestEvents() {
        runTest {
            val storage = InMemoryQueueStorage()
            val queue = EventQueue(
                storage = storage,
                config = OrbitConfig(baseUrl = "https://example.com", maxQueueSize = 2, maxEventAge = 100.hours),
            )

            queue.enqueue(envelope = testEnvelope(name = "a"), enqueuedAtMillis = 0)
            queue.enqueue(envelope = testEnvelope(name = "b"), enqueuedAtMillis = 1)
            queue.enqueue(envelope = testEnvelope(name = "c"), enqueuedAtMillis = 2)

            val evicted = queue.evictExpired(nowMillis = 2)

            assertEquals(expected = 1, actual = evicted)
            assertEquals(expected = listOf("b", "c"), actual = storage.readAll().map { it.envelope.eventName })
        }
    }
}
