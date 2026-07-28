package com.jaidensiu.orbit.destination

import com.jaidensiu.orbit.queue.QueuedEvent

internal sealed interface DeliveryOutcome {
    data object Success : DeliveryOutcome
    data class RetryableFailure(val reason: String, val cause: Throwable? = null) : DeliveryOutcome
    data class PermanentFailure(val reason: String, val cause: Throwable? = null) : DeliveryOutcome
}

internal interface Destination {
    val id: String
    suspend fun send(batch: List<QueuedEvent>): Map<String, DeliveryOutcome>
}
