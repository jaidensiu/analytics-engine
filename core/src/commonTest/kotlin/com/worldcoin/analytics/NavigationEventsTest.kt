package com.worldcoin.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

/** Pins the wire format of navigation events so the schema can't drift. */
class NavigationEventsTest {

    @Test
    fun tabClickedWireFormat() {
        val expected = mapOf(
            Tab.Home to "home",
            Tab.Apps to "apps",
            Tab.Wallet to "wallet",
            Tab.Contacts to "contacts",
            Tab.WorldId to "world_id",
        )

        Tab.entries.forEach { tab ->
            val event = TabClicked(tab)
            assertEquals("tab_clicked", event.name)
            assertEquals(mapOf<String, Any>("tab" to expected.getValue(tab)), event.properties)
        }
    }
}
