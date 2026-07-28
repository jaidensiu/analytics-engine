package com.jaidensiu.orbit.platform

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

internal actual object PlatformPaths {
    actual fun resolve(subdirectory: String): String {
        @Suppress("UNCHECKED_CAST")
        val searchPaths = NSSearchPathForDirectoriesInDomains(
            directory = NSCachesDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        ) as List<String>
        val cachesDir = searchPaths.firstOrNull() ?: NSTemporaryDirectory()
        return "$cachesDir/$subdirectory"
    }
}
