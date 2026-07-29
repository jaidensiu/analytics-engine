package com.jaidensiu.orbit.catalog

import kotlin.test.Test
import kotlin.test.assertEquals

class EventsTest {
    @Test
    fun testTabClicked() {
        WorldIdTab.entries.forEach { tab ->
            val event = TabClicked(tab = tab)
            assertEquals(expected = "tab_clicked", actual = event.name)
            assertEquals(expected = mapOf("tab" to tab.value), actual = event.properties)
        }
    }
}
