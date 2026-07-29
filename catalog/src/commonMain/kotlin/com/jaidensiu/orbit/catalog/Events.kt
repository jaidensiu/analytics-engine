package com.jaidensiu.orbit.catalog


data class TabClicked(val tab: WorldIdTab) : AnalyticsEvent {
    override val name = "tab_clicked"
    override val properties = mapOf("tab" to tab.value)
}
