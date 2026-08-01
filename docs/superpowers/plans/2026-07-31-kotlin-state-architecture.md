# Kotlin State Architecture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the Java single-module API with a Kotlin multi-module SDK based on coroutines, StateFlow, SharedFlow, and complete Android AAR packaging.

**Architecture:** `serialport-core` is pure Kotlin and owns session state, commands, events, queueing, and transport contracts. `serialport-android` adapts the existing JNI library and packages ABIs. `serialport-proxy` forwards core byte streams over TCP without duplicating serial logic.

**Tech Stack:** Kotlin, kotlinx-coroutines, Android Library, Gradle Kotlin DSL, Android Gradle Plugin, StateFlow/SharedFlow, Dokka, JUnit.

---

### Task 1: Modernize Gradle And Module Graph

**Files:**
- Modify: `settings.gradle` or replace with `settings.gradle.kts`
- Modify: root build configuration
- Create: `serialport-core/build.gradle.kts`
- Create: `serialport-android/build.gradle.kts`
- Create: `serialport-proxy/build.gradle.kts`
- Modify: `gradle.properties`

- [ ] Configure Kotlin, Android Library, coroutines, Dokka, Java 17 toolchains, compile SDK, and module dependencies.
- [ ] Set `VERSION_NAME=2.0.0` and publish coordinates for each module.
- [ ] Remove the old application/library module setup and make the three new modules the only published modules.

### Task 2: Implement Core Domain And State Machine

**Files:**
- Create: `serialport-core/src/main/kotlin/io/github/relinran/serialport/core/SerialConfig.kt`
- Create: `SerialState.kt`, `SerialEvent.kt`, `SerialCommand.kt`, `SerialError.kt`, `CommandResult.kt`
- Create: `Transport.kt`, `SerialSession.kt`
- Create tests under `serialport-core/src/test/kotlin`

- [ ] Implement immutable config, bounded queue policy, command handles, and typed errors.
- [ ] Implement a coroutine session with one reader, one writer, serialized state transitions, idempotent disconnect, and terminal close.
- [ ] Add JVM tests for connect, send, timeout, cancellation, overflow, failures, and close.

### Task 3: Implement Android Transport And Diagnostics

**Files:**
- Create: `serialport-android/src/main/kotlin/io/github/relinran/serialport/android/AndroidSerialTransport.kt`
- Create: `AndroidSerialSessionFactory.kt`, `AndroidDeviceScanner.kt`, `PermissionDiagnostic.kt`
- Move/adapt: existing JNI C sources and ABI libraries
- Create Android tests with fake transport

- [ ] Wrap native open/read/write/close operations behind the core `Transport` contract.
- [ ] Load `libserial.so` safely and map missing device, permission, ABI, and native failures to `SerialError`.
- [ ] Package all four native ABIs in the Android AAR and expose device scanning as a Flow.

### Task 4: Implement TCP Proxy Module

**Files:**
- Create: `serialport-proxy/src/main/kotlin/io/github/relinran/serialport/proxy/SerialProxy.kt`
- Create: `ProxyConfig.kt`, `ProxyState.kt`, `ProxyEvent.kt`
- Create tests under `serialport-proxy/src/test/kotlin`

- [ ] Implement a coroutine TCP server that forwards client bytes to `SerialSession` and received serial bytes back to clients.
- [ ] Expose proxy state as StateFlow and failures as typed events.
- [ ] Test loopback forwarding, client disconnect, stop, and serial failure.

### Task 5: Publishable Documentation And Verification

**Files:**
- Replace: `README.md`, `README_zh.md`
- Modify: `.github/workflows/release.yml`
- Create: consumer ProGuard rules, source/Javadoc publication configuration

- [ ] Document the new Kotlin API, StateFlow collection in Compose, dependency coordinates, ABI packaging, permissions, and lifecycle.
- [ ] Change release automation to build and upload all module AARs for `v2.0.0` tags.
- [ ] Run unit tests, Android checks, AAR inspections, ABI checks, Markdown checks, and a clean release build.
- [ ] Commit, push, tag `v2.0.0`, and monitor the remote workflow.
