# serial-api-android

English | [简体中文](README_zh.md)

`serial-api-android` is a Kotlin-first Android serial communication SDK for Linux serial devices. It ships as one Android Library module and exposes a protocol-neutral API in `io.android.serial.api`.

## Features

- Coroutine-based serial sessions with `StateFlow` and `SharedFlow`
- Ordered write queue, write timeout, retry, and queue overflow policies
- Optional request/response matching with per-command response timeout
- Configurable automatic reconnect with exponential backoff
- Incremental frame parsing for fragmented and coalesced reads
- Configurable header, footer, length field, byte order, checksum range, and size limits
- Built-in XOR-8, SUM-8, and CRC16-Modbus checksums plus custom checksum support
- Kotlin extensions for hexadecimal, numeric, byte-order, and bit conversions
- Pluggable TX/RX diagnostics logging
- JNI serial transport with `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64` binaries
- TCP proxy support

## Requirements

- Android API 26+
- Kotlin/JVM target 17
- No Compose dependency. Compose applications can collect the exposed flows directly.

## Install

The recommended distribution is the AAR attached to a GitHub Release. Download `serial-api-android-<version>.aar` and place it in your application's `libs` directory:

```kotlin
repositories {
    flatDir { dirs("libs") }
}

dependencies {
    implementation(files("libs/serial-api-android-2.0.0.aar"))
}
```

The project is also Maven-publish ready. When a Maven repository is configured for the project, use:

```kotlin
implementation("io.github.relinran:serial-api-android:2.0.0")
```

## Quick start

```kotlin
import io.android.serial.api.SerialConfig
import io.android.serial.api.SerialPortApi
import io.android.serial.api.SerialMode

val session = SerialPortApi.create(applicationContext)
val connected = session.connect(
    SerialConfig(
        path = "/dev/ttyS4",
        baudRate = 115_200,
        mode = SerialMode.ReadWrite,
        readBufferSize = 512
    )
)

if (connected.isSuccess) {
    val handle = session.send(SerialCommand(byteArrayOf(0x01, 0x03, 0x00, 0x00)))
    val result = handle.completion.await()
}
```

Call `disconnect()` for a reusable session. Call `close()` when the session is permanently disposed.

## Observe state and events

```kotlin
session.state.collect { state ->
    // Idle, Connecting, Connected, Closing, Failed, or Closed
}

session.events.collect { event ->
    when (event) {
        is SerialEvent.DataReceived -> consume(event.data)
        is SerialEvent.ErrorRaised -> report(event.error)
        is SerialEvent.Reconnecting -> reportRetry(event.attempt, event.delayMillis)
        SerialEvent.ReconnectSucceeded -> reportRecovered()
        SerialEvent.ReconnectExhausted -> reportDisconnected()
        else -> Unit
    }
}
```

## Request and response

Protocol matching remains application-configurable:

```kotlin
val handle = session.send(
    SerialCommand(
        payload = request,
        responseMatcher = ResponseMatchers.prefix(byteArrayOf(0xAA.toByte())),
        responseTimeout = Duration.ofSeconds(2),
        maxRetries = 2,
        retryDelay = Duration.ofMillis(100)
    )
)

when (val result = handle.completion.await()) {
    is CommandResult.Response -> consume(result.data)
    is CommandResult.TimedOut -> retryAtProtocolLevel()
    is CommandResult.Failed -> report(result.error)
    else -> Unit
}
```

Implement `ResponseMatcher` for device-specific command IDs, addresses, sequence numbers, or any other protocol rule.

## Frame parsing

`SerialFrameParser` handles half frames, sticky packets, noise before a header, size limits, footer checks, and optional checksums:

```kotlin
val parser = SerialFrameParser(
    FrameConfig(
        header = byteArrayOf(0x55, 0xAA.toByte()),
        lengthOffset = 2,
        lengthSize = 2,
        lengthByteOrder = ByteOrder.LITTLE_ENDIAN,
        footer = byteArrayOf(0x0D, 0x0A),
        checksum = Checksums.Crc16Modbus,
        maximumFrameSize = 2048
    )
)

val parsed = parser.offerDetailed(incomingBytes)
parsed.frames.forEach(::consumeFrame)
parsed.errors.forEach(::reportParseError)
```

Custom checksums implement `Checksum`:

```kotlin
val checksum = Checksum { bytes -> byteArrayOf(bytes.sumOf { it.toInt() and 0xFF }.toByte()) }
```

## Byte utilities

All conversions are Kotlin extensions and support both byte orders:

```kotlin
val order = ByteOrder.LITTLE_ENDIAN
val encoded = 0x12345678.toByteArray(order)
val number = encoded.toInt(order = order)
val bits = encoded.toBooleanArray(order)
val oneByte = bits.copyOf(8).toByte(order)
val raw = "01 FF 10".hexToByteArray()
```

Supported conversions include `Short`, `Int`, `Float`, `Double`, hexadecimal strings, concatenation, and single/multiple-byte bit arrays.

## Reconnect and logging

```kotlin
val config = SerialConfig(
    path = "/dev/ttyS4",
    baudRate = 115_200,
    reconnect = ReconnectPolicy(
        enabled = true,
        maxAttempts = 5,
        initialDelay = Duration.ofMillis(500),
        maxDelay = Duration.ofSeconds(10)
    )
)

val session = SerialPortApi.create(
    context = applicationContext,
    logger = SerialLogger { direction, data, timestamp ->
        println("$timestamp $direction ${data.toHex(\" \")}")
    }
)
```

## Build locally

```shell
./gradlew :library:testDebugUnitTest :library:assembleRelease
```

The local AAR is generated at `library/build/outputs/aar/library-release.aar`.

## GitHub Actions release

The workflow in `.github/workflows/release.yml` runs for tags matching `v*.*.*`. The tag must match `VERSION_NAME` in `gradle.properties` exactly:

```shell
git tag v2.0.0
git push origin v2.0.0
```

The workflow runs unit tests, builds the release AAR, verifies the native ABI files, and creates a GitHub Release with the downloadable asset:

```text
serial-api-android-2.0.0.aar
```

## License and disclaimer

Code is available under the [MIT License](LICENSE). Read [DISCLAIMER.md](DISCLAIMER.md) before controlling physical equipment or privileged device nodes.
