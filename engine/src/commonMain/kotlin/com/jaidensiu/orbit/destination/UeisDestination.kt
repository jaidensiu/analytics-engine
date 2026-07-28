package com.jaidensiu.orbit.destination

import com.jaidensiu.orbit.queue.QueuedEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

internal class UeisDestination(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : Destination {
    override val id: String = "ueis"

    override suspend fun send(batch: List<QueuedEvent>): Map<String, DeliveryOutcome> {
        val outcomes = mutableMapOf<String, DeliveryOutcome>()
        for ((index, event) in batch.withIndex()) {
            val outcome = attempt(event = event)
            outcomes[event.id] = outcome
            if (outcome is DeliveryOutcome.RetryableFailure) {
                batch.drop(n = index + 1).forEach { skipped ->
                    outcomes[skipped.id] =
                        DeliveryOutcome.RetryableFailure(reason = "skipped after an earlier failure in this batch")
                }
                break
            }
        }
        return outcomes
    }

    private suspend fun attempt(event: QueuedEvent): DeliveryOutcome {
        return try {
            val response = httpClient.post(urlString = "$baseUrl/v1/events") {
                contentType(type = ContentType.Application.Json)
                setBody(body = event.envelope)
            }
            classify(response = response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DeliveryOutcome.RetryableFailure(reason = "network error: ${e.message}", cause = e)
        }
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
