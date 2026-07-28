package com.jaidensiu.orbit.destination

import com.jaidensiu.orbit.AuthTokenProvider
import com.jaidensiu.orbit.OrbitConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun createOrbitHttpClient(
    config: OrbitConfig,
    authTokenProvider: AuthTokenProvider,
): HttpClient {
    return HttpClient {
        expectSuccess = false

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.callTimeout.inWholeMilliseconds
            connectTimeoutMillis = config.callTimeout.inWholeMilliseconds
            socketTimeoutMillis = config.callTimeout.inWholeMilliseconds
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val token = authTokenProvider.currentToken() ?: return@loadTokens null
                    BearerTokens(accessToken = token, refreshToken = "")
                }
                refreshTokens {
                    val token = authTokenProvider.currentToken() ?: return@refreshTokens null
                    BearerTokens(accessToken = token, refreshToken = "")
                }
            }
        }

        install(Logging) {
            level = LogLevel.INFO
        }
    }
}
