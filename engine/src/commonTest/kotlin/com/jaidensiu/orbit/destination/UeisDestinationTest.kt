package com.jaidensiu.orbit.destination

import com.jaidensiu.orbit.testing.testQueuedEvent
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UeisDestinationTest {

    private fun clientRespondingWith(status: HttpStatusCode): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = "{}",
                        status = status,
                        headers = headersOf(name = HttpHeaders.ContentType, value = "application/json"),
                    )
                }
            }
            install(ContentNegotiation) { json() }
        }
    }

    @Test
    fun successfulResponseIsClassifiedAsSuccess() {
        runTest {
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.OK),
                baseUrl = "https://ueis.example.com",
            )

            val outcomes = destination.send(batch = listOf(testQueuedEvent(id = "1", name = "a")))

            assertEquals(expected = DeliveryOutcome.Success, actual = outcomes["1"])
        }
    }

    @Test
    fun serverErrorIsClassifiedAsRetryable() {
        runTest {
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.InternalServerError),
                baseUrl = "https://ueis.example.com",
            )

            val outcomes = destination.send(batch = listOf(testQueuedEvent(id = "1", name = "a")))

            assertIs<DeliveryOutcome.RetryableFailure>(value = outcomes.getValue(key = "1"))
        }
    }

    @Test
    fun rateLimitIsClassifiedAsRetryable() {
        runTest {
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.TooManyRequests),
                baseUrl = "https://ueis.example.com",
            )

            val outcomes = destination.send(batch = listOf(testQueuedEvent(id = "1", name = "a")))

            assertIs<DeliveryOutcome.RetryableFailure>(value = outcomes.getValue(key = "1"))
        }
    }

    @Test
    fun clientErrorIsClassifiedAsPermanent() {
        runTest {
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.BadRequest),
                baseUrl = "https://ueis.example.com",
            )

            val outcomes = destination.send(batch = listOf(testQueuedEvent(id = "1", name = "a")))

            assertIs<DeliveryOutcome.PermanentFailure>(value = outcomes.getValue(key = "1"))
        }
    }

    @Test
    fun aRetryableFailureSkipsTheRemainingEventsInTheBatch() {
        runTest {
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.InternalServerError),
                baseUrl = "https://ueis.example.com",
            )

            val outcomes = destination.send(
                batch = listOf(testQueuedEvent(id = "1", name = "a"), testQueuedEvent(id = "2", name = "b")),
            )

            assertIs<DeliveryOutcome.RetryableFailure>(value = outcomes.getValue(key = "1"))
            assertIs<DeliveryOutcome.RetryableFailure>(value = outcomes.getValue(key = "2"))
        }
    }
}
