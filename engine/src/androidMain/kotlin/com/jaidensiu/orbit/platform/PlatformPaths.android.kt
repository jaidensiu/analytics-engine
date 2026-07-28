package com.jaidensiu.orbit.platform

import android.content.Context
import java.io.File

internal actual object PlatformPaths {
    private var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
    }

    actual fun resolve(subdirectory: String): String {
        val context = checkNotNull(value = appContext) {
            "PlatformPaths.install(context) must be called before Orbit is configured on Android " +
                "-- use the Orbit(context, config, authTokenProvider) factory function."
        }
        return File(context.filesDir, subdirectory).absolutePath
    }
}
