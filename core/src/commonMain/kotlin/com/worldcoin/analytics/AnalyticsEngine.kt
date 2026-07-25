package com.worldcoin.analytics

class AnalyticsEngine(
    private val sink: EventSink,
    private val onError: (Throwable) -> Unit = { println("[warn] AnalyticsEngine: sink failed to track event: $it") },
) {

    fun track(event: AnalyticsEvent) {
        try {
            sink.track(event.name, event.properties)
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
