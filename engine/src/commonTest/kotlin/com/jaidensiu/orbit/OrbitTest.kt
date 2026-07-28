package com.jaidensiu.orbit

import com.jaidensiu.orbit.catalog.TabClicked
import com.jaidensiu.orbit.catalog.WorldIdTab
import com.jaidensiu.orbit.destination.DeliveryOutcome
import com.jaidensiu.orbit.destination.Destination
import com.jaidensiu.orbit.testing.FakeClock
import com.jaidensiu.orbit.testing.FakeDestination
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class OrbitTest {

    private val queueDirectory = "orbit-test-${Random.nextInt()}"

    @AfterTest
    fun cleanup() {
        val queuedFile = Path(queueDirectory, "fake.jsonl")
        if (SystemFileSystem.exists(path = queuedFile)) {
            SystemFileSystem.delete(path = queuedFile)
        }
        val directory = Path(queueDirectory)
        if (SystemFileSystem.exists(path = directory)) {
            SystemFileSystem.delete(path = directory)
        }
    }

    private fun childScopeOf(testScope: TestScope): CoroutineScope {
        return CoroutineScope(
            context = testScope.coroutineContext + SupervisorJob(parent = testScope.coroutineContext[Job]),
        )
    }

    private fun buildOrbit(destination: Destination, scope: CoroutineScope): Orbit {
        return Orbit(
            config = OrbitConfig(baseUrl = "https://example.com", flushInterval = 50.milliseconds),
            destinations = listOf(destination),
            queueDirectory = queueDirectory,
            httpClient = HttpClient(MockEngine) {
                engine {
                    addHandler { respondOk() }
                }
            },
            clock = FakeClock(),
            scope = scope,
        )
    }

    @Test
    fun trackDeliversTheEventThroughToTheDestination() {
        runTest {
            val received = mutableListOf<String>()
            val destination = FakeDestination(id = "fake") { batch ->
                received += batch.map { it.envelope.eventName }
                batch.associate { it.id to DeliveryOutcome.Success }
            }
            val orbit = buildOrbit(destination = destination, scope = childScopeOf(testScope = this))

            orbit.track(event = TabClicked(tab = WorldIdTab.Apps))
            advanceTimeBy(delayTimeMillis = 100.milliseconds.inWholeMilliseconds)
            runCurrent()

            assertEquals(expected = listOf("tab_clicked"), actual = received)

            orbit.shutdown()
        }
    }

    @Test
    fun addDeliveryFailureListenerRegistersSynchronouslyBeforeAnyCoroutineDispatch() {
        runTest {
            val destination = FakeDestination(id = "fake") { batch ->
                batch.associate { it.id to DeliveryOutcome.PermanentFailure(reason = "rejected") }
            }
            val orbit = buildOrbit(destination = destination, scope = childScopeOf(testScope = this))

            val failures = mutableListOf<DeliveryFailure>()
            orbit.addDeliveryFailureListener(listener = { failures += it })

            orbit.track(event = TabClicked(tab = WorldIdTab.Apps))
            advanceTimeBy(delayTimeMillis = 100.milliseconds.inWholeMilliseconds)
            runCurrent()

            assertEquals(expected = 1, actual = failures.size)
            assertTrue(actual = failures.single().dropped)

            orbit.shutdown()
        }
    }

    @Test
    fun shutdownStopsFurtherDeliveryAttempts() {
        runTest {
            var attempts = 0
            val destination = FakeDestination(id = "fake") { batch ->
                attempts++
                batch.associate { it.id to DeliveryOutcome.Success }
            }
            val orbit = buildOrbit(destination = destination, scope = childScopeOf(testScope = this))

            orbit.shutdown()

            orbit.track(event = TabClicked(tab = WorldIdTab.Apps))
            advanceTimeBy(delayTimeMillis = 100.milliseconds.inWholeMilliseconds)
            runCurrent()

            assertEquals(expected = 0, actual = attempts)
        }
    }
}
