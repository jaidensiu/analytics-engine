package com.jaidensiu.orbit.queue

import com.jaidensiu.orbit.testing.testQueuedEvent
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import platform.Foundation.NSTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class FileQueueStorageIosTest {

    private fun uniqueTestPath(): Path {
        return Path(NSTemporaryDirectory(), "orbit-test-${Random.nextInt()}.jsonl")
    }

    @Test
    fun aSecondInstanceAtTheSamePathSeesEventsAppendedByTheFirst() {
        runTest {
            val path = uniqueTestPath()

            val first = FileQueueStorage(path = path)
            first.append(event = testQueuedEvent(id = "1", name = "a"))
            first.append(event = testQueuedEvent(id = "2", name = "b"))

            val second = FileQueueStorage(path = path)
            val events = second.readAll()

            assertEquals(expected = listOf("a", "b"), actual = events.map { it.envelope.eventName })
        }
    }

    @Test
    fun removalsPersistAcrossInstances() {
        runTest {
            val path = uniqueTestPath()

            val first = FileQueueStorage(path = path)
            first.append(event = testQueuedEvent(id = "1", name = "a"))
            first.append(event = testQueuedEvent(id = "2", name = "b"))
            first.removeIds(ids = setOf("1"))

            val second = FileQueueStorage(path = path)

            assertEquals(expected = listOf("b"), actual = second.readAll().map { it.envelope.eventName })
        }
    }
}
