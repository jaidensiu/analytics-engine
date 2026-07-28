# Orbit

A Kotlin Multiplatform SDK that routes analytics events into the Unified Events Ingestion Service
(UEIS), shared between Android and iOS.

## Modules

- **`:catalog`** -- pure Kotlin, zero dependencies. Just the event definitions (`AnalyticsEvent`
  and its concrete event classes, e.g. `TabClicked`). Depend on this alone if you only need the
  event types (e.g. for testing).
- **`:engine`** -- the `Orbit` facade and everything needed to deliver events reliably: a durable,
  disk-backed queue per destination, batched flush, exponential backoff with jitter, and a circuit
  breaker that pauses sending to a destination after repeated failures instead of hammering it.
  Orbit owns its own HTTP transport (Ktor) -- it does not depend on the host app's networking
  stack -- so the same reliability guarantees apply identically on Android and iOS.

## Usage

Build one `Orbit` instance and hold it as a singleton in your own app's DI graph (Orbit does not
enforce a static/global instance itself).

**Android** (e.g. in `Application.onCreate`):

```kotlin
val orbit = Orbit(
    context = applicationContext,
    config = OrbitConfig(baseUrl = "https://ueis.example.com"),
    authTokenProvider = { sessionManager.currentAccessToken },
)
```

**iOS:**

```kotlin
val orbit = Orbit(
    config = OrbitConfig(baseUrl = "https://ueis.example.com"),
    authTokenProvider = { SessionManager.shared.currentAccessToken },
)
```

Then, from either platform:

```kotlin
orbit.bottomNavTabClicked(tab = WorldIdTab.ForHumans)
orbit.addDeliveryFailureListener { failure -> /* observe dropped/retried events */ }
```

Every public method on `Orbit` is a plain, non-suspend call -- no `Flow`/`suspend` is exposed on
the API surface, since Kotlin/Native bridges those to Swift as completion-handler/collector shapes
that need extra tooling (e.g. SKIE) to feel native. `track()`/`bottomNavTabClicked()` etc. enqueue
and return immediately; delivery happens off that call. `addDeliveryFailureListener` takes a plain
callback for the same reason, and bridges to a normal Swift trailing closure.

## Reliability notes

- Events are durably queued to disk before being sent, so a killed/backgrounded process doesn't
  lose anything queued before the crash -- only its redelivery timing, since retry/circuit-breaker
  state itself is in-memory and resets on process restart.
- Each destination (UEIS today; more can be added later via the internal `Destination` interface)
  has its own queue and circuit breaker, so one destination being down never blocks or drops events
  bound for another.
- Permanently rejected events (4xx) are dropped and reported via `addDeliveryFailureListener`, not
  silently discarded.
