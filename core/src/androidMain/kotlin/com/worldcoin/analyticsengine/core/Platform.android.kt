package com.worldcoin.analyticsengine.core

import android.os.Build

actual fun platformName(): String = "Android API ${Build.VERSION.SDK_INT}"
