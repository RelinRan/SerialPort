# Single Library Architecture Design

## Goal

Collapse the Kotlin state architecture into one Android Library and one public package: `android.serial.port.api`.

## Structure

The repository publishes one Gradle module named `library` and one artifact named `serialport`. It contains the coroutine session, immutable state and event models, Android JNI transport, device discovery, native ABI libraries, and TCP proxy.

All public types use `android.serial.port.api`. Implementation-only types are `internal` and organized by focused source files rather than separate artifacts. Compose remains an optional consumer of `StateFlow`; the library does not depend on Compose Runtime.

## User API

Users add one dependency, create a session through `SerialPort.create(context)`, collect `state` and `events`, and call `connect`, `send`, `disconnect`, or `close`. TCP proxy support is available from the same artifact.

## Distribution

The release workflow builds and publishes `serialport-2.0.0.aar`. The AAR includes Kotlin bytecode, consumer rules, sources metadata, and `libserial.so` for all supported ABIs.

## Removal

The separate `serialport-core`, `serialport-android`, and `serialport-proxy` modules are removed after their source and tests have been migrated. The legacy `app` module remains outside the build until repository policy permits its separate cleanup.
