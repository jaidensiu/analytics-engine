package com.jaidensiu.orbit.catalog

abstract class BaseAnalytics(
    eventName: String,
    properties: Map<String, String> = emptyMap(),
    publicKeyId: String = "",
    anonymizedUserId: String = "",
    operatingSystem: String = "",
    sessionId: String = "",
    appIdentifier: String = "",
    appVersion: String = "",
    builtType: String = "",
)

internal abstract class UnauthenticatedUserEvent(
    preLoginUserId: String,
    eventName: String
) : BaseAnalytics(
    eventName = eventName
)

internal abstract class UserEvent(
    publicKeyId: String = "",
    eventName: String
) : BaseAnalytics()

internal abstract class AnonymizedEvent(
    anonymizedUserId: String = ""
) : BaseAnalytics()
