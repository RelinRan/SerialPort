# SerialPort

English | [简体中文](README_zh.md)

SerialPort is a Kotlin-first Android SDK for Linux serial devices. Version 2 uses coroutines, `StateFlow`, and `SharedFlow` instead of callbacks, handlers, and manually managed threads. It is designed for Jetpack Compose state collection without taking a Compose Runtime dependency.

The complete SDK is delivered as one Android Library. Its public API, state models, device scanner, JNI transport, and TCP proxy all live in `android.serial.port.api`.

## Install

```kotlin
dependencies {
    implementation("io.github.relinran:serialport:2.0.0")
}
```

The Android AAR packages `libserial.so` for `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`. The minimum Android API is 26.

## Create a session

```kotlin
val session = SerialPortApi.create(applicationContext)

val result = session.connect(
    SerialConfig(
        path = "/dev/ttyS4",
        baudRate = 115_200,
        mode = SerialMode.ReadWrite,
        readBufferSize = 512
    )
)
```

`SerialSession` owns one read task and one ordered write task. It is safe to call `connect()` or `disconnect()` repeatedly. `close()` is terminal and releases the session resources.

## Observe state in Compose

The SDK does not require Compose. A Compose screen can consume state directly:

```kotlin
val state by session.state.collectAsStateWithLifecycle()

LaunchedEffect(session) {
    session.events.collect { event ->
        when (event) {
            is SerialEvent.DataReceived -> consume(event.data)
            is SerialEvent.ErrorRaised -> showError(event.error)
            else -> Unit
        }
    }
}
```

`state` is replayable and represents the current session: `Idle`, `Connecting`, `Connected`, `Closing`, `Failed`, or `Closed`. `events` contains one-time byte, command, timeout, and error notifications.

## Send bytes

```kotlin
val handle = session.send(
    SerialCommand(
        payload = byteArrayOf(0x01, 0x03, 0x00, 0x00),
        timeout = 500.milliseconds,
        tag = "read-registers"
    )
)

when (val outcome = handle.completion.await()) {
    is CommandResult.Sent -> log("sent ${outcome.bytes} bytes")
    is CommandResult.TimedOut -> retry()
    is CommandResult.Failed -> showError(outcome.error)
    CommandResult.Cancelled -> Unit
}
```

Queue capacity, overflow behavior, default write delay, and default timeout are configured through `QueueConfig`. The SDK transports raw byte streams. Protocol framing, checksums, request matching, retry policy, and hardware safety controls belong to the application layer.

## Device access

`AndroidDeviceScanner` exposes device candidates as a Flow. A device node must be readable and writable by the application. Do not rely on root or globally writable permissions for production deployments; configure device ownership and SELinux policy in the firmware instead.

## Build

```shell
./gradlew :library:testDebugUnitTest :library:assembleRelease
```

Push a `v2.0.0` tag to publish `serialport-2.0.0.aar` through GitHub Actions.

## License

Repository-owned code is available under the [MIT License](LICENSE). Read the [Software Disclaimer](DISCLAIMER.md) before controlling physical equipment or using privileged device nodes.
