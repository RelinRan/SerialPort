# Android SerialPort

[English](README.md) | 简体中文

SerialPort 是面向 Android 应用的 Linux 串口通信库，适用于 `/dev/ttyS*`、`/dev/ttyUSB*` 及厂商自定义 UART 设备路径。项目包含打开设备所需的 JNI 桥接和原生库，并在 Java 层提供异步读写、发送队列、事件回调与超时处理。

仓库还提供串口设备发现、二进制转换工具及可选的串口转 TCP 代理服务。

## 核心逻辑

`Serial<T>` 是主要入口。每个实例管理一个串口连接和独立的读写任务，并通过 `SerialHandler` 分发事件。未显式传入 Handler 的构造方法使用 Android 主线程 Looper，因此监听回调默认在主线程执行。

待发送数据包保存在延迟队列中，调用方无需阻塞即可控制连续指令的发送间隔。每个成功入队的数据包都会生成 ID，也可以携带业务自定义的 `options`；发送和超时回调会返回相同的数据包信息。

原生库支持以下 ABI：

- `arm64-v8a`
- `armeabi-v7a`
- `x86`
- `x86_64`

Android 模块最低支持 API 21。

## 添加依赖

### JitPack

在 `dependencyResolutionManagement` 中添加 JitPack：

```groovy
repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

然后添加对应标签版本：

```groovy
dependencies {
    implementation 'com.github.RelinRan:SerialPort:v1.0.0'
}
```

### JAR 方式

每个 GitHub Release 都会提供带版本号的 JAR，例如 `serial-1.0.0.jar`。该文件只包含 Java 字节码，Android 应用仍需在所支持的每个 ABI 目录下放置对应的 `libserial.so`：

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

建议优先使用 JitPack 依赖，因为它会保留 Android Library 打包结构，并把原生库放到 Android 期望的位置。

## 打开串口

使用设备路径、波特率、访问模式和可选的读取缓冲区大小创建 `Serial`：

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

建议在调用 `open()` 前注册监听：

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
    throw new IllegalStateException("无法打开串口设备");
}
```

`CommandContext` 可以替换为任意业务类型；不需要数据包上下文时可使用 `Serial<Object>`。

## 发送指令

串口已打开时，`send` 会返回生成的数据包 ID；串口未打开时不会入队，并返回空字符串。

```java
byte[] request = new byte[] {0x01, 0x03, 0x00, 0x00, 0x00, 0x02};

String packetId = serial.send(request);
String delayedId = serial.send(request, 250L);
String contextualId = serial.send(request, new CommandContext("read-registers"));
String explicitId = serial.send(request, new CommandContext("read-registers"), 250L);
```

没有显式指定延迟的重载方法使用配置的发送间隔，默认值为 100 ms。显式延迟也会与队列中已有的数据包串行计算，因此指令会保持调度顺序。

数据写入后，每个监听器都会收到 `onSerialSend`。库会按配置的超时时间安排超时回调，默认值为 500 ms；在超时窗口内收到数据会移除待处理的超时消息。库本身不会解析协议，也不会把响应与某个请求逐一匹配，分帧、校验、请求关联、重试和半包缓存都应由应用层实现。

## 正确处理接收数据

读取任务会把每次成功读取的数据复制为新的字节数组再回调。一次回调不一定等于一个协议帧：一个帧可能分多次到达，多帧也可能合并到一次回调中。应用必须按照设备协议累积并解析字节流。

默认读取缓冲区为 64 字节。如果设备可能突发返回更多数据，应使用四参数构造方法设置更大的缓冲区。

## 生命周期

监听所属组件销毁时应移除监听。后续可能重新打开串口时调用 `close()`；实例永久不再使用时调用 `release()`：

```java
serial.remove(listenerId);
serial.close();

// 仅用于最终释放，调用后不要复用该实例。
serial.release();
```

`release()` 会清空工作线程、队列、Handler 和数据流，因此不能在释放后继续使用同一实例打开串口。

## 查找设备路径

`SerialPortFinder` 读取 `/proc/tty/drivers` 并扫描对应的设备路径：

```java
SerialPortFinder finder = new SerialPortFinder();

for (String path : finder.getAllDevicesPath()) {
    Log.d("Serial", path);
}
```

设备发现只是辅助能力，不代表应用具有访问权限。Android 硬件厂商经常使用固定的自定义 UART 路径，因此生产应用通常从设备配置中读取串口路径。

## 二进制转换

`Bytecode` 支持基础数值、字节数组、十六进制字符串、位数组和端序转换：

```java
Bytecode codec = new Bytecode();

byte[] littleEndian = codec.toBytes(9600);
byte[] bigEndian = codec.toBytes(9600, ByteOrder.BIG_ENDIAN);
String hex = codec.toHex(new byte[] {0x01, 0x2A});
int value = codec.toInt(littleEndian);
```

数值转换默认使用小端序。`Bytecode` 实例会复用内部格式化缓冲区，不应由多个线程并发共享。

## 串口转 TCP 代理

`Sercd` 通过后台服务把串口设备暴露到指定网络接口和端口。使用前声明服务和网络权限：

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

// 注销广播接收器并停止服务。
proxy.stop();
```

本库不提供身份认证和传输加密。没有在外层增加安全措施时，不要把代理暴露到不可信网络。

## 设备权限

只有应用进程可读写的设备节点才能被原生接口打开。权限不足时，当前实现会尝试运行 `/system/bin/su` 并执行 `chmod 666 <device>`。

这种回退方式不适合许多生产环境：它依赖 Root，可能被 SELinux 阻止，还会让所有进程都能写入该设备节点。更合理的部署方式是在设备固件中配置所有者、用户组和 SELinux 策略，使应用只获得必要权限。

不要使用不可信输入拼接串口设备路径。

## 构建与发布

项目使用 Android Gradle Plugin 7.0.2、Gradle 7.2、Java 8 源码兼容级别、compile SDK 31 和 minimum SDK 21。

```shell
./gradlew :app:assembleRelease
```

Release AAR 输出到 `app/build/outputs/aar/app-release.aar`。

版本遵循语义化版本规则。推送 `v1.0.0` 形式的标签后，发布工作流会校验标签与 `VERSION_NAME`、构建 AAR、提取 `classes.jar`，并在 GitHub Releases 中发布为 `serial-1.0.0.jar`。

## 能力边界与问题反馈

SerialPort 只负责传输层访问，不定义消息分帧、设备协议、校验算法、请求响应关联、重连策略、身份认证或硬件安全控制。

反馈问题时请提供 Android 版本、设备型号、ABI、串口路径、波特率、访问模式、权限状态及最小可复现通信过程。提交日志前请移除凭据和敏感业务数据。

## 开源协议

仓库所有者拥有版权的代码按 [MIT License](LICENSE) 发布。部分文件保留独立版权和许可证声明，相应内容继续适用这些声明。将本库用于物理设备或特权设备前，请阅读[软件声明](DISCLAIMER_zh.md)。
