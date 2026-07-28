package com.jaidensiu.orbit.dispatch

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class CircuitBreakerConfig(
    val failureThreshold: Int = 5,
    val openDuration: Duration = 30.seconds,
    val maxOpenDuration: Duration = 5.minutes,
)

internal sealed interface CircuitState {
    data object Closed : CircuitState
    data object HalfOpen : CircuitState
    data class Open(val until: Instant) : CircuitState
}

internal class CircuitBreaker(
    private val config: CircuitBreakerConfig = CircuitBreakerConfig(),
    private val clock: Clock = Clock.System,
) {
    private val mutex = Mutex()
    private var state: CircuitState = CircuitState.Closed
    private var consecutiveFailures = 0
    private var currentOpenDuration = config.openDuration

    suspend fun canAttempt(): Boolean = mutex.withLock {
        when (val current = state) {
            CircuitState.Closed -> true
            CircuitState.HalfOpen -> false
            is CircuitState.Open -> {
                if (clock.now() >= current.until) {
                    state = CircuitState.HalfOpen
                    true
                } else {
                    false
                }
            }
        }
    }

    suspend fun recordSuccess() = mutex.withLock {
        state = CircuitState.Closed
        consecutiveFailures = 0
        currentOpenDuration = config.openDuration
    }

    suspend fun recordFailure() = mutex.withLock {
        consecutiveFailures++
        when (state) {
            CircuitState.HalfOpen -> {
                currentOpenDuration = (currentOpenDuration * 2).coerceAtMost(maximumValue = config.maxOpenDuration)
                state = CircuitState.Open(until = clock.now() + currentOpenDuration)
            }
            CircuitState.Closed -> if (consecutiveFailures >= config.failureThreshold) {
                state = CircuitState.Open(until = clock.now() + currentOpenDuration)
            }
            is CircuitState.Open -> Unit
        }
    }
}
