package com.jaidensiu.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationEventsTest {

    @Test
    fun tabClickedWireFormat() {
        val expected = mapOf(
            BottomNavTab.ForHumans to "for_humans",
            BottomNavTab.Credentials to "credentials",
            BottomNavTab.Apps to "apps",
            BottomNavTab.Notifications to "notifications",
        )

        BottomNavTab.entries.forEach { tab ->
            val event = TabClicked(tab)
            assertEquals("tab_clicked", event.name)
            assertEquals(mapOf<String, Any>("tab" to expected.getValue(tab)), event.properties)
        }
    }
}
