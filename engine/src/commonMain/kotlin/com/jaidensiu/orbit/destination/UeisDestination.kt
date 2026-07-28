package com.jaidensiu.orbit.destination

import com.jaidensiu.orbit.queue.EventEnvelope
import com.jaidensiu.orbit.queue.QueuedEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

@Serializable
internal data class BatchEnvelope(val batch: List<EventEnvelope>)

internal class UeisDestination(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : Destination {
    override val id: String = "ueis"

    override suspend fun send(batch: List<QueuedEvent>): Map<String, DeliveryOutcome> {
        if (batch.isEmpty()) return emptyMap()
        val outcome = attempt(batch = batch)
        return batch.associate { it.id to outcome }
    }

    private suspend fun attempt(batch: List<QueuedEvent>): DeliveryOutcome {
        return try {
            val response = httpClient.post(urlString = "$baseUrl/v1/batch") {
                contentType(type = ContentType.Application.Json)
                header(key = "Idempotency-Key", value = idempotencyKeyFor(batch = batch))
                setBody(body = BatchEnvelope(batch = batch.map { it.envelope }))
            }
            classify(response = response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DeliveryOutcome.RetryableFailure(reason = "network error: ${e.message}", cause = e)
        }
    }

    private fun idempotencyKeyFor(batch: List<QueuedEvent>): String {
        return batch.joinToString(separator = ",") { it.id }
    }

    private fun classify(response: HttpResponse): DeliveryOutcome {
        return when {
            response.status.isSuccess() -> {
                DeliveryOutcome.Success
            }
            response.status == HttpStatusCode.TooManyRequests -> {
                DeliveryOutcome.RetryableFailure(reason = "rate limited: ${response.status}")
            }
            response.status.value in 500..599 -> {
                DeliveryOutcome.RetryableFailure(reason = "server error: ${response.status}")
            }
            else -> {
                DeliveryOutcome.PermanentFailure(reason = "rejected: ${response.status}")
            }
        }
    }
}
