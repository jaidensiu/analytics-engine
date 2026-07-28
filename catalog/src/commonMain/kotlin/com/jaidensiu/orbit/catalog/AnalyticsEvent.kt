package com.jaidensiu.orbit.catalog

sealed interface AnalyticsEvent {
    val name: String
    val properties: Map<String, String>
        get() {
            return emptyMap()
        }
}
