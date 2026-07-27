package com.jaidensiu.analytics

enum class Tab(val value: String) {
    Home(value = "home"),
    Apps(value = "apps"),
    Wallet(value = "wallet"),
    Contacts(value = "contacts"),
    WorldId(value = "world_id"),
}

data class TabClicked(val tab: Tab) : AnalyticsEvent {
    override val name get() = "tab_clicked"
    override val properties get() = mapOf<String, Any>("tab" to tab.value)
}
