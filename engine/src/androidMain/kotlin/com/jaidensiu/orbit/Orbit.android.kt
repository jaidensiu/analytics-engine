package com.jaidensiu.orbit

import android.content.Context
import com.jaidensiu.orbit.destination.UeisDestination
import com.jaidensiu.orbit.destination.createOrbitHttpClient
import com.jaidensiu.orbit.platform.PlatformPaths

fun Orbit(
    context: Context,
    config: OrbitConfig,
    authTokenProvider: AuthTokenProvider,
): Orbit {
    PlatformPaths.install(context = context)
    val httpClient = createOrbitHttpClient(config = config, authTokenProvider = authTokenProvider)
    return Orbit(
        config = config,
        destinations = listOf(UeisDestination(httpClient = httpClient, baseUrl = config.baseUrl)),
        queueDirectory = PlatformPaths.resolve(subdirectory = "orbit"),
        httpClient = httpClient,
    )
}
