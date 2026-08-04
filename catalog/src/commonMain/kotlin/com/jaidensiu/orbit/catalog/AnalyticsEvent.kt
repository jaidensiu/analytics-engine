package com.jaidensiu.orbit.catalog

interface AnalyticsEvent {
    val name: String
    val properties: Map<String, String>
        get() = emptyMap()
}

abstract class BaseAnalytics(
    override val name: String,
    override val properties: Map<String, String> = emptyMap(),
    val operatingSystem: String? = null,
    val sessionId: String? = null,
    val appIdentifier: String? = null,
    val appVersion: String? = null,
    val buildType: String? = null,
) : AnalyticsEvent {
    init {
        require(name.isNotBlank()) { "event name must not be blank" }
    }
}

abstract class UnauthenticatedUserEvent internal constructor(
    name: String,
    properties: Map<String, String> = emptyMap(),
    val preLoginUserId: String,
) : BaseAnalytics(name = name, properties = properties)

abstract class UserEvent internal constructor(
    name: String,
    properties: Map<String, String> = emptyMap(),
    val publicKeyId: String? = null,
) : BaseAnalytics(name = name, properties = properties)

abstract class AnonymizedEvent internal constructor(
    name: String,
    properties: Map<String, String> = emptyMap(),
    val anonymizedUserId: String? = null,
) : BaseAnalytics(name = name, properties = properties)
