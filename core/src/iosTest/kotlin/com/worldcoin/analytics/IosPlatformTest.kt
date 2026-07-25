package com.worldcoin.analytics

import kotlin.test.Test
import kotlin.test.assertTrue

class IosPlatformTest {

    @Test
    fun platformNameIdentifiesIos() {
        assertTrue(platformName().startsWith("iOS"))
    }
}
