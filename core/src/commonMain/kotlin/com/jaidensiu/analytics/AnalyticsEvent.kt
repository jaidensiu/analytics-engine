package com.jaidensiu.analytics

sealed interface AnalyticsEvent {
    val name: String
    val properties: Map<String, Any> get() = emptyMap()
}
