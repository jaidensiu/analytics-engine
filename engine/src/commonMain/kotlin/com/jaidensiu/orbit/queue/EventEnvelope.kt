package com.jaidensiu.orbit.queue

import com.jaidensiu.orbit.OrbitConfig
import com.jaidensiu.orbit.SDK_VERSION
import com.jaidensiu.orbit.catalog.AnalyticsEvent
import com.jaidensiu.orbit.catalog.CATALOG_VERSION
import com.jaidensiu.orbit.platform.platformName
import kotlinx.serialization.Serializable

@Serializable
internal data class EventEnvelope(
    val eventName: String,
    val timestampMillis: Long,
    val userId: String? = null,
    val deviceId: String? = null,
    val anonymousId: String? = null,
    val properties: Map<String, String>,
    val platform: String = "unknown",
    val sdkVersion: String = "unknown",
    val catalogVersion: String = "unknown",
    val appVersion: String? = null,
)

internal fun AnalyticsEvent.toEnvelope(config: OrbitConfig, timestampMillis: Long): EventEnvelope {
    return EventEnvelope(
        eventName = name,
        timestampMillis = timestampMillis,
        userId = config.userId,
        deviceId = config.deviceId,
        anonymousId = config.anonymousId,
        properties = properties,
        platform = platformName,
        sdkVersion = SDK_VERSION,
        catalogVersion = CATALOG_VERSION,
        appVersion = config.appVersion,
    )
}
