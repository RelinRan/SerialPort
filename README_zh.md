# SerialPort

简体中文 | [English](README.md)

SerialPort 是用于访问 Linux 串口设备的 Android 通信库。项目通过 JNI 操作设备节点，并提供发送队列、延迟指令、事件回调、设备发现、字节转换工具及可选的串口转网络代理。

## 功能

- 按指定波特率和访问模式打开 Android/Linux 串口设备节点。
- 支持 `arm64-v8a`、`armeabi-v7a`、`x86` 和 `x86_64`。
- 支持发送队列和延迟发送。
- 提供发送、接收及超时回调。
- 发送数据包可携带业务自定义参数。
- 通过 `/proc/tty/drivers` 查找串口驱动和设备路径。
- 提供小端序及自定义端序的字节转换工具。
- 提供可选的串口转网络代理服务。

## 环境要求

- Android API 21 或更高版本。
- 设备内核已暴露兼容的 Linux 串口设备节点。
- 应用具有该设备节点的读写权限。普通权限不足时，库可能尝试执行 `su` 和 `chmod 666`。

不同硬件厂商采用的设备路径、权限策略、内核和 SELinux 规则可能不同。正式部署前必须在每一种目标设备上完成验证。

## 接入方式

### JitPack

在 `settings.gradle` 中添加 JitPack 仓库：

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

在应用模块中添加依赖：

```groovy
dependencies {
    implementation 'com.github.RelinRan:SerialPort:v1.0.0'
}
```

### Release JAR 与原生库

GitHub Releases 提供的 `serial-1.0.0.jar` 只包含 Java 类。将 JAR 放入模块的 `libs` 目录，并从本仓库复制所需 ABI 对应的原生库：

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

普通 JAR 无法安装 JNI 原生库。应用支持的每种 ABI 都必须包含对应的 `.so` 文件。

## 快速开始

查找可用串口设备路径：

```java
SerialPortFinder finder = new SerialPortFinder();
for (String path : finder.getAllDevicesPath()) {
    Log.i("SerialPort", "device: " + path);
}
```

创建并打开串口连接：

```java
Serial serial = new Serial("/dev/ttyMSM2", 115200, SerialMode.RDWR);
serial.setDebug(BuildConfig.DEBUG);

long listenerId = serial.addSerialListener(new OnSerialListener<Object>() {
    @Override
    public void onSerialSend(SerialPacket<Object> packet) {
        // 数据包已写入输出流。
    }

    @Override
    public void onSerialTimeout(SerialPacket<Object> packet) {
        // 在超时前未收到可取消超时的接收事件。
    }

    @Override
    public void onSerialReceived(byte[] data) {
        // 处理串口返回数据。
    }
});

serial.open();
serial.send(new byte[] {0x01, 0x02, 0x03});
```

也可以延迟发送并携带业务参数：

```java
long delayMillis = 200L;
Object options = new Object();
serial.send(new byte[] {0x01, 0x02}, options, delayMillis);
```

页面或组件销毁时应移除监听，然后关闭并释放串口：

```java
serial.remove(listenerId);
serial.close();
serial.release();
```

不要在多个线程之间共享同一个 `Bytecode` 实例，其格式化方法会复用内部缓冲区。

## 串口转网络代理

在 `AndroidManifest.xml` 中声明服务：

```xml
<service
    android:name="android.serial.port.api.SercdService"
    android:directBootAware="true"
    android:enabled="true" />
```

使用串口路径、网络接口地址和监听端口启动代理：

```java
Sercd sercd = new Sercd(context);
Map<String, String> interfaces = sercd.feedNetworkInterfacesList();
String address = interfaces.get("eth0");
sercd.start("/dev/ttyMSM2", address, 30001);
```

不再使用代理时调用 `sercd.stop()`。使用此功能时需声明 `android.permission.INTERNET`。

## 构建

项目使用 Gradle 7.2 和 Android Gradle Plugin 7.0.2。配置本地 Android SDK 后执行：

```shell
./gradlew :app:assembleRelease
```

AAR 输出路径为 `app/build/outputs/aar/app-release.aar`。

## 发布与版本规则

SerialPort 遵循[语义化版本](https://semver.org/lang/zh-CN/)。推送 `vMAJOR.MINOR.PATCH` 格式的标签后，GitHub Actions 会验证标签与 `VERSION_NAME` 一致，构建 release AAR、提取 Java 类，并在 GitHub Releases 中发布带版本号的 JAR。

## 安全注意事项

访问硬件设备节点及修改节点权限可能降低设备安全性。不要将不可信输入作为设备路径，生产系统也不应依赖全局可写权限。串口指令可能直接影响外接设备，应用层必须根据具体协议实现分帧、校验、超时及设备安全保护。

接入前请阅读[软件声明](DISCLAIMER_zh.md)。

## 参与贡献

欢迎提交 Issue 和范围清晰的 Pull Request。报告设备相关问题时，请提供 Android 版本、硬件型号、ABI、串口设备路径、波特率、相关日志和最小复现方式。

## 开源协议

Copyright (c) 2026 RelinRan。仓库所有者拥有版权的代码采用 [MIT License](LICENSE) 开源；带有独立许可证声明的文件仍适用其原有声明。
