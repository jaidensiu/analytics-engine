package com.jaidensiu.orbit.destination

import com.jaidensiu.orbit.testing.testQueuedEvent
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UeisDestinationTest {

    private fun clientRespondingWith(
        status: HttpStatusCode,
        onRequest: (HttpRequestData) -> Unit = {},
    ): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    onRequest(request)
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
    fun theWholeBatchSharesTheSingleRequestOutcome() {
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

    @Test
    fun emptyBatchSendsNoRequestAndReturnsNoOutcomes() {
        runTest {
            var requestCount = 0
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.OK) { requestCount++ },
                baseUrl = "https://ueis.example.com",
            )

            val outcomes = destination.send(batch = emptyList())

            assertEquals(expected = 0, actual = requestCount)
            assertEquals(expected = emptyMap(), actual = outcomes)
        }
    }

    @Test
    fun sendsASingleRequestToTheBatchEndpoint() {
        runTest {
            var requestCount = 0
            var requestedPath = ""
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.OK) { request ->
                    requestCount++
                    requestedPath = request.url.encodedPath
                },
                baseUrl = "https://ueis.example.com",
            )

            destination.send(
                batch = listOf(testQueuedEvent(id = "1", name = "a"), testQueuedEvent(id = "2", name = "b")),
            )

            assertEquals(expected = 1, actual = requestCount)
            assertEquals(expected = "/v1/batch", actual = requestedPath)
        }
    }

    @Test
    fun sendsAnIdempotencyKeyDerivedFromTheQueuedEventIds() {
        runTest {
            var idempotencyKey: String? = null
            val destination = UeisDestination(
                httpClient = clientRespondingWith(status = HttpStatusCode.OK) { request ->
                    idempotencyKey = request.headers["Idempotency-Key"]
                },
                baseUrl = "https://ueis.example.com",
            )

            destination.send(
                batch = listOf(testQueuedEvent(id = "1", name = "a"), testQueuedEvent(id = "2", name = "b")),
            )

            assertEquals(expected = "1,2", actual = idempotencyKey)
        }
    }
}
