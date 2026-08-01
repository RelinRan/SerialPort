# serial-api-android

[English](README.md) | 简体中�?
SerialPort 是面�?Linux 串口设备�?Kotlin 优先 Android SDK�?.x 版本使用协程、`StateFlow` �?`SharedFlow` 替代回调、Handler 和手动线程管理；它可直接�?Jetpack Compose 收集状态，�?SDK 本身不依�?Compose Runtime�?
完整 SDK 以一�?Android Library 交付。公开 API、状态模型、设备扫描、JNI 传输�?TCP 代理统一位于 `io.android.serial.api`�?
## 接入

```kotlin
dependencies {
    implementation("io.github.relinran:serialport:2.0.0")
}
```

Android AAR 内置 `arm64-v8a`、`armeabi-v7a`、`x86`、`x86_64` �?`libserial.so`。最低支�?Android API 26�?
## 创建会话

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

`SerialSession` 管理一个读取任务和一个顺序写入任务。`connect()`、`disconnect()` 可安全重复调用；`close()` 是终结操作，会释放会话资源�?
## Compose 状态消�?
SDK 不强制依�?Compose。Compose 页面可以直接收集状态：

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

`state` 是可重放的当前会话状态：`Idle`、`Connecting`、`Connected`、`Closing`、`Failed` �?`Closed`。`events` 用于接收一次性的字节、命令、超时和错误事件�?
## 发送字�?
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

通过 `QueueConfig` 配置队列容量、溢出策略、默认发送延迟和默认超时。SDK 只传输原始字节流；协议分帧、校验和、请求匹配、重试策略和硬件安全控制由应用层负责�?
## 设备访问

`AndroidDeviceScanner` �?Flow 暴露候选设备。设备节点必须可由应用读取和写入。生产固件不应依�?Root 或全局可写权限，应通过设备所有者、用户组�?SELinux 策略授予最小权限�?
## 构建

```shell
./gradlew :library:testDebugUnitTest :library:assembleRelease
```

推�?`v2.0.0` 标签后，GitHub Actions 会发�?`serialport-2.0.0.aar`�?
## 开源协�?
仓库所有者拥有版权的代码采用 [MIT License](LICENSE) 开源。控制物理设备或访问特权设备节点前，请阅读[软件声明](DISCLAIMER_zh.md)�?