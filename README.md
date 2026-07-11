# SerialPort

[简体中文](README_zh.md) | English

SerialPort is an Android library for communicating with Linux serial devices. It combines JNI-based device access with queued writes, delayed commands, callbacks, device discovery, byte conversion utilities, and an optional serial-to-network proxy.

## Features

- Opens Android/Linux serial device nodes with configurable baud rate and access mode.
- Supports `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`.
- Queues outgoing packets and supports delayed transmission.
- Provides send, receive, and timeout callbacks.
- Allows optional application data to travel with each packet.
- Discovers serial drivers and device paths through `/proc/tty/drivers`.
- Includes little-endian and configurable-endian byte conversion helpers.
- Provides an optional serial-to-network proxy service.

## Requirements

- Android API 21 or later.
- A device that exposes a compatible Linux serial device node.
- Read and write permission for that device node. The library may attempt `su` and `chmod 666` when ordinary access is unavailable.

Hardware vendors frequently use different device paths, permission policies, kernels, and SELinux rules. Validate the library on every target device before deployment.

## Installation

### JitPack

Add JitPack to the dependency repositories in `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your application module:

```groovy
dependencies {
    implementation 'com.github.RelinRan:SerialPort:v1.0.0'
}
```

### Release JAR and native libraries

GitHub Releases provides `serial-1.0.0.jar`, which contains the Java classes only. Copy the JAR to your module's `libs` directory and add the required native libraries from this repository to the matching ABI directories:

```text
app/
  libs/serial-1.0.0.jar
  src/main/jniLibs/arm64-v8a/libserial.so
  src/main/jniLibs/armeabi-v7a/libserial.so
  src/main/jniLibs/x86/libserial.so
  src/main/jniLibs/x86_64/libserial.so
```

```groovy
dependencies {
    implementation files('libs/serial-1.0.0.jar')
}
```

A plain JAR cannot install JNI libraries. Include at least the `.so` file for every ABI shipped by your application.

## Quick Start

Discover available serial device paths:

```java
SerialPortFinder finder = new SerialPortFinder();
for (String path : finder.getAllDevicesPath()) {
    Log.i("SerialPort", "device: " + path);
}
```

Create and open a serial connection:

```java
Serial serial = new Serial("/dev/ttyMSM2", 115200, SerialMode.RDWR);
serial.setDebug(BuildConfig.DEBUG);

long listenerId = serial.addSerialListener(new OnSerialListener<Object>() {
    @Override
    public void onSerialSend(SerialPacket<Object> packet) {
        // The packet was written to the output stream.
    }

    @Override
    public void onSerialTimeout(SerialPacket<Object> packet) {
        // No receive event cancelled the packet timeout.
    }

    @Override
    public void onSerialReceived(byte[] data) {
        // Process bytes from the serial device.
    }
});

serial.open();
serial.send(new byte[] {0x01, 0x02, 0x03});
```

Delayed packets and application-specific options are also supported:

```java
long delayMillis = 200L;
Object options = new Object();
serial.send(new byte[] {0x01, 0x02}, options, delayMillis);
```

Remove listeners when their owner is destroyed, then close and release the connection:

```java
serial.remove(listenerId);
serial.close();
serial.release();
```

Do not share one `Bytecode` instance between threads. Its formatting methods reuse an internal buffer.

## Serial-to-Network Proxy

Declare the service in `AndroidManifest.xml`:

```xml
<service
    android:name="android.serial.port.api.SercdService"
    android:directBootAware="true"
    android:enabled="true" />
```

Start the proxy with the serial path, network interface address, and listening port:

```java
Sercd sercd = new Sercd(context);
Map<String, String> interfaces = sercd.feedNetworkInterfacesList();
String address = interfaces.get("eth0");
sercd.start("/dev/ttyMSM2", address, 30001);
```

Call `sercd.stop()` when the proxy is no longer needed. Add `android.permission.INTERNET` when using this feature.

## Building

The project uses Gradle 7.2 and Android Gradle Plugin 7.0.2. Configure a local Android SDK, then run:

```shell
./gradlew :app:assembleRelease
```

The AAR is written to `app/build/outputs/aar/app-release.aar`.

## Releases and Versioning

SerialPort follows [Semantic Versioning](https://semver.org/). A tag named `vMAJOR.MINOR.PATCH` starts the GitHub Actions release workflow. The workflow verifies that the tag matches `VERSION_NAME`, builds the release AAR, extracts its Java classes, and publishes a versioned JAR in GitHub Releases.

## Security and Safety

Opening hardware device nodes and changing their permissions can weaken device security. Never run untrusted input as a device path, and do not rely on world-writable permissions in production images. Serial commands can affect attached equipment; use framing, validation, timeouts, and device-specific safety controls in the application layer.

Read the [Software Disclaimer](DISCLAIMER.md) before integrating this library.

## Contributing

Issues and focused pull requests are welcome. Include the Android version, hardware model, ABI, serial device path, baud rate, relevant logs, and a minimal reproduction when reporting device-specific failures.

## License

Copyright (c) 2026 RelinRan. Repository-owned code is available under the [MIT License](LICENSE). Files that contain their own license headers remain governed by those notices.
