package com.jaidensiu.orbit.platform

internal expect object PlatformPaths {
    fun resolve(subdirectory: String): String
}
