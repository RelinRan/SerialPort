# Kotlin State Architecture Design

## Goal

Replace the Java-era single-module serial library with a Kotlin, coroutine-based, StateFlow-first SDK that is portable for Android applications and does not depend on Compose Runtime.

## Non-goals

- No Java API compatibility layer.
- No Compose sample application in the SDK repository.
- No protocol-specific framing, checksum, request matching, or reconnect policy in the transport core.

## Modules

`serialport-core` is a pure Kotlin/JVM module. It owns `SerialSession`, immutable configuration, command/result types, state and event models, queue policy, transport interfaces, and error taxonomy. It has no Android, JNI, Compose, or network dependency.

`serialport-android` is an Android Library. It implements the core transport using the existing native serial library, provides ABI packaging, Android device discovery, and structured permission diagnostics. It exposes `AndroidSerialSessionFactory` for application use.

`serialport-proxy` is an Android Library that reuses a core session and forwards bytes over a TCP server. It owns proxy configuration, client lifecycle, and proxy state, without duplicating serial I/O.

Dependencies point inward: Android and proxy depend on core; core depends on neither.

## State And Events

The session exposes `StateFlow<SerialState>` for replayable state and `SharedFlow<SerialEvent>` for one-shot events. States are `Idle`, `Connecting`, `Connected`, `Closing`, `Failed`, and `Closed`. Events include received bytes, completed commands, timeouts, state changes, and typed errors.

All public operations are coroutine-friendly. `send` returns a `CommandHandle` with an ID and `Deferred<CommandResult>`. The API uses structured `SerialError` values rather than leaking platform exceptions.

## Concurrency And Lifecycle

Each session owns a `SupervisorJob` scope, a single writer consuming a bounded `Channel`, and one reader task. State mutations are serialized by a coordinator. Queue overflow is configurable as reject or drop-oldest. Connect and disconnect are idempotent; close is terminal and cancels pending work, closes the transport, and releases native resources.

## Android Packaging

The primary artifact is a complete AAR containing Kotlin bytecode, native `.so` files for `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`, consumer rules, POM metadata, source, and Dokka documentation. The new architecture starts at version `2.0.0`. Compose consumers use `collectAsStateWithLifecycle()` without adding Compose to SDK dependencies.

## Verification

Core tests cover state transitions, queue overflow, timeout, cancellation, and error mapping. Android tests cover fake transport integration, device diagnostics, ABI loading, and resource release. Proxy tests cover loopback forwarding and disconnects. Release verification inspects all AARs, native entries, consumer rules, POM metadata, sources, and Dokka output.
