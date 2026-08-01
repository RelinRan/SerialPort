# serial-api-android

[English](README.md) | 简体中文

`serial-api-android` 是面向 Linux 串口设备的 Kotlin 优先 Android 串口通信 SDK。项目以一个 Android Library 模块交付，公开 API 统一位于 `io.android.serial.api`。

## 主要能力

- 协程、`StateFlow`、`SharedFlow` 驱动的串口会话
- 有序写队列、写超时、重试和队列溢出策略
- 可选请求/响应匹配、响应超时和自动重连
- 支持半包、粘包、帧头、帧尾、长度字段、大小端和校验的增量帧解析
- 内置 XOR-8、SUM-8、CRC16-Modbus 和自定义校验
- 十六进制、Short、Int、Float、Double 和 Boolean 位数组 Kotlin 扩展
- 可插拔 TX/RX 日志，支持四种 JNI ABI

## 安装

推荐下载 GitHub Release 中的 `serial-api-android-<版本>.aar`，放入应用 `libs` 目录：

```kotlin
dependencies {
    implementation(files("libs/serial-api-android-2.0.0.aar"))
}
```

也可使用 Maven 坐标：

```kotlin
implementation("io.github.relinran:serial-api-android:2.0.0")
```

## 快速开始

```kotlin
val session = SerialPortApi.create(applicationContext)
val connected = session.connect(
    SerialConfig("/dev/ttyS4", 115_200, mode = SerialMode.ReadWrite)
)

if (connected.isSuccess) {
    val result = session.send(SerialCommand(byteArrayOf(0x01, 0x03, 0x00, 0x00)))
        .completion.await()
}
```

## 请求响应

协议规则由业务层配置：

```kotlin
val result = session.send(
    SerialCommand(
        payload = request,
        responseMatcher = ResponseMatchers.prefix(byteArrayOf(0xAA.toByte())),
        responseTimeout = Duration.ofSeconds(2),
        maxRetries = 2
    )
).completion.await()
```

## 帧解析

```kotlin
val parser = SerialFrameParser(
    FrameConfig(
        header = byteArrayOf(0x55, 0xAA.toByte()),
        lengthOffset = 2,
        lengthSize = 2,
        lengthByteOrder = ByteOrder.LITTLE_ENDIAN,
        footer = byteArrayOf(0x0D, 0x0A),
        checksum = Checksums.Crc16Modbus
    )
)

val parsed = parser.offerDetailed(incomingBytes)
parsed.frames.forEach(::consumeFrame)
parsed.errors.forEach(::reportParseError)
```

## 字节工具

```kotlin
val order = ByteOrder.LITTLE_ENDIAN
val encoded = 0x12345678.toByteArray(order)
val number = encoded.toInt(order = order)
val bits = encoded.toBooleanArray(order)
val oneByte = bits.copyOf(8).toByte(order)
val raw = "01 FF 10".hexToByteArray()
```

## 自动重连和日志

```kotlin
val config = SerialConfig(
    path = "/dev/ttyS4",
    baudRate = 115_200,
    reconnect = ReconnectPolicy(enabled = true, maxAttempts = 5)
)

val session = SerialPortApi.create(
    context = applicationContext,
    logger = SerialLogger { direction, data, timestamp ->
        println("$timestamp $direction ${data.toHex(" ")}")
    }
)
```

## 构建和发布

```shell
./gradlew :library:testDebugUnitTest :library:assembleRelease
```

本地 AAR：`library/build/outputs/aar/library-release.aar`。

推送与 `VERSION_NAME` 一致的标签即可发布：

```shell
git tag v2.0.0
git push origin v2.0.0
```

GitHub Actions 会运行测试、构建并校验 AAR，然后创建 GitHub Release，提供 `serial-api-android-2.0.0.aar` 下载。

## 协议

代码采用 [MIT License](LICENSE)。使用前请阅读 [软件声明](DISCLAIMER_zh.md)。