# SerialPort for Android

English | [简体中文](README_zh.md)

SerialPort is a small Android library for applications that communicate with Linux serial device nodes such as `/dev/ttyS*`, `/dev/ttyUSB*`, or vendor-specific UART paths. It provides the JNI bridge and native binaries needed to open a device, plus an asynchronous Java API for queued writes, reads, callbacks, and timeouts.

The repository also includes serial-device discovery, binary conversion helpers, and an optional TCP proxy service.

## What the library does

`Serial<T>` is the primary API. It owns a serial connection, runs dedicated read and write workers, and delivers events through a `SerialHandler`. Constructors that do not receive a handler use Android's main looper, so listener callbacks are delivered on the main thread by default.

Outgoing packets are stored in a delay queue. This makes it possible to enforce spacing between commands without blocking the caller. Each accepted packet receives an ID and may carry an application-defined `options` value that is returned with send and timeout callbacks.

Supported native ABIs:

- `arm64-v8a`
- `armeabi-v7a`
- `x86`
- `x86_64`

The Android module requires API 21 or newer.

## Add the library

### JitPack

Add JitPack to `dependencyResolutionManagement`:

```groovy
repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

Then add the tagged release:

```groovy
dependencies {
    implementation 'com.github.RelinRan:SerialPort:v1.0.0'
}
```

### JAR distribution

Every GitHub Release contains a versioned JAR, for example `serial-1.0.0.jar`. That file contains Java bytecode only. Android still needs the matching `libserial.so` file under each ABI directory your application supports:

```text
app/
├── libs/
│   └── serial-1.0.0.jar
└── src/main/jniLibs/
    ├── arm64-v8a/libserial.so
    ├── armeabi-v7a/libserial.so
    ├── x86/libserial.so
    └── x86_64/libserial.so
```

```groovy
dependencies {
    implementation files('libs/serial-1.0.0.jar')
}
```

Use the JitPack dependency when possible. It preserves the Android library packaging and includes native libraries in the expected layout.

## Open a serial connection

Create a `Serial` instance with a device path, baud rate, access mode, and optional read-buffer size:

```java
Serial<CommandContext> serial = new Serial<>(
        "/dev/ttyS4",
        115200,
        SerialMode.RDWR,
        256
);

serial.setInterval(100L);
serial.setTimeout(500L);
serial.setDebug(BuildConfig.DEBUG);
```

Register the listener before calling `open()`:

```java
long listenerId = serial.addSerialListener(new OnSerialListener<CommandContext>() {
    @Override
    public void onSerialSend(SerialPacket<CommandContext> packet) {
        Log.d("Serial", "sent: " + packet.getId());
    }

    @Override
    public void onSerialReceived(byte[] data) {
        consumeFrame(data);
    }

    @Override
    public void onSerialTimeout(SerialPacket<CommandContext> packet) {
        retryOrFail(packet.getOptions());
    }
});

serial.open();
if (!serial.isOpen()) {
    throw new IllegalStateException("Unable to open the serial device");
}
```

`CommandContext` is any application type you choose. Use `Serial<Object>` when no packet metadata is needed.

## Send commands

`send` returns a generated packet ID when the connection is open. It returns an empty string when the packet was not accepted because the connection is closed.

```java
byte[] request = new byte[] {0x01, 0x03, 0x00, 0x00, 0x00, 0x02};

String packetId = serial.send(request);
String delayedId = serial.send(request, 250L);
String contextualId = serial.send(request, new CommandContext("read-registers"));
String explicitId = serial.send(request, new CommandContext("read-registers"), 250L);
```

The overload without an explicit delay uses the configured interval, which defaults to 100 ms. Explicit delays are also serialized against packets already in the queue, so commands retain their scheduled order.

After a packet is written, each listener receives `onSerialSend`. A timeout callback is scheduled using the configured timeout, which defaults to 500 ms. Incoming data inside that timeout window removes pending timeout messages. The library does not parse protocols or match a response to an individual request; framing, checksums, correlation, retries, and partial-frame buffering belong in the application layer.

## Receive data safely

The read worker emits each successful stream read as a new byte array. One callback is not guaranteed to equal one protocol frame: a frame may arrive in several callbacks, and several frames may arrive together. Accumulate bytes and decode them according to the connected device's protocol.

The default read buffer is 64 bytes. Choose a larger value through the four-argument constructor when bursts may exceed that size.

## Lifecycle

Remove listeners when their owner is destroyed. Close the port when it may be opened again, or release the instance permanently when it will no longer be used:

```java
serial.remove(listenerId);
serial.close();

// Final cleanup only; do not reuse the instance afterward.
serial.release();
```

Do not call `release()` and then attempt to reopen the same instance because its workers, queue, handler, and streams are cleared.

## Find device paths

`SerialPortFinder` reads `/proc/tty/drivers` and scans matching device prefixes:

```java
SerialPortFinder finder = new SerialPortFinder();

for (String path : finder.getAllDevicesPath()) {
    Log.d("Serial", path);
}
```

Discovery is a convenience, not a permission check. Android builds from device manufacturers often expose UARTs through fixed custom paths, so production applications commonly obtain the path from device configuration.

## Binary values

`Bytecode` converts primitive values, byte arrays, hexadecimal strings, bit arrays, and byte order:

```java
Bytecode codec = new Bytecode();

byte[] littleEndian = codec.toBytes(9600);
byte[] bigEndian = codec.toBytes(9600, ByteOrder.BIG_ENDIAN);
String hex = codec.toHex(new byte[] {0x01, 0x2A});
int value = codec.toInt(littleEndian);
```

Default numeric conversion uses little-endian order. A `Bytecode` instance reuses an internal formatting buffer and must not be shared concurrently between threads.

## Serial-to-TCP proxy

`Sercd` starts a background service that exposes a serial device over a network interface and port. Declare the service and Internet permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application>
    <service
        android:name="android.serial.port.api.SercdService"
        android:directBootAware="true"
        android:enabled="true" />
</application>
```

```java
Sercd proxy = new Sercd(context);
proxy.setOnSercdListener(state -> Log.d("Sercd", state.name()));

Map<String, String> interfaces = proxy.feedNetworkInterfacesList();
String address = interfaces.get("eth0");
proxy.start("/dev/ttyS4", address, 30001);

// Unregisters the receiver and stops the service.
proxy.stop();
```

Do not expose the proxy to an untrusted network without authentication and transport protection implemented outside this library.

## Device permissions

The native port can open only device nodes readable and writable by the application process. When access is denied, the current implementation attempts to run `/system/bin/su` and execute `chmod 666 <device>`.

That fallback is unsuitable for many production environments: it requires root, may be blocked by SELinux, and makes the device node writable by every process. The preferred deployment model is to configure ownership, groups, SELinux policy, or vendor firmware so the application receives only the access it needs.

Never build a device path from untrusted input.

## Build and release

The repository uses Android Gradle Plugin 7.0.2, Gradle 7.2, Java 8 source compatibility, compile SDK 31, and minimum SDK 21.

```shell
./gradlew :app:assembleRelease
```

The release AAR is written to `app/build/outputs/aar/app-release.aar`.

Versions follow Semantic Versioning. Pushing a tag such as `v1.0.0` runs the release workflow, verifies the tag against `VERSION_NAME`, builds the AAR, extracts `classes.jar`, and publishes it as `serial-1.0.0.jar` in GitHub Releases.

## Scope and support

SerialPort provides transport-level access. It does not define message framing, device protocols, checksums, request/response correlation, reconnection policy, authentication, or hardware safety controls.

When reporting a problem, include the Android version, device model, ABI, serial path, baud rate, access mode, permission state, and a minimal reproducible exchange. Remove credentials and sensitive payloads from logs.

## License

Repository-owned code is distributed under the [MIT License](LICENSE). Some files retain separate copyright and license notices; those notices continue to govern the corresponding material. Review the [Software Disclaimer](DISCLAIMER.md) before using the library with physical equipment or privileged devices.
