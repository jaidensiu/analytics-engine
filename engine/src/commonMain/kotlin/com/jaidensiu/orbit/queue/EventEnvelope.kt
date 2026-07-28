package com.jaidensiu.orbit.queue

import com.jaidensiu.orbit.OrbitConfig
import com.jaidensiu.orbit.catalog.AnalyticsEvent
import kotlinx.serialization.Serializable

@Serializable
internal data class EventEnvelope(
    val eventName: String,
    val timestampMillis: Long,
    val userId: String? = null,
    val deviceId: String? = null,
    val anonymousId: String? = null,
    val properties: Map<String, String>,
)

internal fun AnalyticsEvent.toEnvelope(config: OrbitConfig, timestampMillis: Long): EventEnvelope {
    return EventEnvelope(
        eventName = name,
        timestampMillis = timestampMillis,
        userId = config.userId,
        deviceId = config.deviceId,
        anonymousId = config.anonymousId,
        properties = properties,
    )
}
