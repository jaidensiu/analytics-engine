package com.jaidensiu.analytics

import kotlin.test.Test
import kotlin.test.assertTrue

class AndroidPlatformTest {

    @Test
    fun platformNameIdentifiesAndroid() {
        assertTrue(platformName().startsWith("Android"))
    }
}
