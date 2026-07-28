package com.jaidensiu.orbit

import com.jaidensiu.orbit.dispatch.CircuitBreakerConfig
import com.jaidensiu.orbit.dispatch.RetryConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

data class OrbitConfig(
    val baseUrl: String,
    val userId: String? = null,
    val deviceId: String? = null,
    val anonymousId: String? = null,
    val maxQueueSize: Int = 1_000,
    val maxEventAge: Duration = 24.hours,
    val batchSize: Int = 20,
    val flushInterval: Duration = 10.seconds,
    val callTimeout: Duration = 15.seconds,
    val retryConfig: RetryConfig = RetryConfig(),
    val circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig(),
)
