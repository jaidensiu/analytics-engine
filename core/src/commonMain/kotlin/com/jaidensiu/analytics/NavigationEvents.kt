package com.jaidensiu.analytics

enum class BottomNavTab(val value: String) {
    Apps(value = "apps"),
    Credentials(value = "credentials"),
    ForHumans(value = "for_humans"),
    Notifications(value = "notifications"),
}

data class TabClicked(val tab: BottomNavTab) : AnalyticsEvent {
    override val name get() = "tab_clicked"
    override val properties get() = mapOf<String, Any>("tab" to tab.value)
}
