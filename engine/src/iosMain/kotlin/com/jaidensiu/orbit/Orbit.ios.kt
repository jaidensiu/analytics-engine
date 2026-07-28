package com.jaidensiu.orbit

import com.jaidensiu.orbit.destination.UeisDestination
import com.jaidensiu.orbit.destination.createOrbitHttpClient
import com.jaidensiu.orbit.platform.PlatformPaths

fun Orbit(config: OrbitConfig, authTokenProvider: AuthTokenProvider): Orbit {
    val httpClient = createOrbitHttpClient(config = config, authTokenProvider = authTokenProvider)
    return Orbit(
        config = config,
        destinations = listOf(UeisDestination(httpClient = httpClient, baseUrl = config.baseUrl)),
        queueDirectory = PlatformPaths.resolve(subdirectory = "orbit"),
        httpClient = httpClient,
    )
}
