package com.jaidensiu.orbit.catalog

data class TabClicked(
    val tab: WorldIdTab,
) : UserEvent(
    name = "tab_clicked",
    properties = mapOf("tab" to tab.name)
)
