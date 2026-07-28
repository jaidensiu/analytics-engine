package com.jaidensiu.orbit.catalog

enum class WorldIdTab(val value: String) {
    Apps(value = "apps"),
    Credentials(value = "credentials"),
    ForHumans(value = "for_humans"),
    Notifications(value = "notifications"),
}

data class TabClicked(val tab: WorldIdTab) : AnalyticsEvent {
    override val name = "tab_clicked"
    override val properties = mapOf("tab" to tab.value)
}
