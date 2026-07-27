package com.jaidensiu.analytics

fun interface EventSink {
    fun track(eventName: String, properties: Map<String, Any>)
}
