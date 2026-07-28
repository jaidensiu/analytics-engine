package com.jaidensiu.orbit

import com.jaidensiu.orbit.catalog.TabClicked
import com.jaidensiu.orbit.catalog.WorldIdTab

fun Orbit.bottomNavTabClicked(tab: WorldIdTab) {
    track(TabClicked(tab = tab))
}
