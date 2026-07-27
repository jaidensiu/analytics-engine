package com.jaidensiu.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

class AnalyticsEngineTest {

    private class RecordingSink : EventSink {
        val events = mutableListOf<Pair<String, Map<String, Any>>>()

        override fun track(eventName: String, properties: Map<String, Any>) {
            events += eventName to properties
        }
    }

    @Test
    fun eventsAreForwardedToTheSink() {
        val sink = RecordingSink()
        val engine = AnalyticsEngine(sink)

        engine.track(TabClicked(tab = Tab.Home))

        assertEquals(listOf("tab_clicked" to mapOf<String, Any>("tab" to "home")), sink.events)
    }

    @Test
    fun sinkFailureIsReportedAndDoesNotPropagate() {
        val failure = IllegalStateException("sink is down")
        val reported = mutableListOf<Throwable>()
        val engine = AnalyticsEngine(
            sink = { _, _ -> throw failure },
            onError = { reported += it },
        )

        engine.track(TabClicked(tab = Tab.Wallet))

        assertEquals(listOf<Throwable>(failure), reported)
    }
}
