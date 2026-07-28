package com.jaidensiu.orbit.queue

import kotlinx.serialization.Serializable

@Serializable
internal data class QueuedEvent(
    val id: String,
    val enqueuedAtMillis: Long,
    val envelope: EventEnvelope,
)
