package com.jaidensiu.orbit.catalog

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationEventsTest {

    @Test
    fun tabClickedHasExpectedNameAndProperties() {
        val event = TabClicked(tab = WorldIdTab.ForHumans)

        assertEquals(expected = "tab_clicked", actual = event.name)
        assertEquals(expected = mapOf("tab" to "for_humans"), actual = event.properties)
    }
}
