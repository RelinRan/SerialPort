###### Serial Port
Android 串口通讯 arm64-v8a、armeabi-v7a、x86、x86_64
###### AAR
|名称|操作|
|-|-|
|serial.jar|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/serial.jar)|
|arm64-v8a|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/arm64-v8a/libserial.so)|
|armeabi-v7a|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/armeabi-v7a/libserial.so)|
|x86|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/x86/libserial.so)|
|x86_64|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/x86_64/libserial.so)|
|arm-zip|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/arm.zip)|
|x86-zip|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/x86.zip)|

###### Maven
1.build.grade | setting.grade
```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```
2./app/build.grade
```
dependencies {
	implementation 'com.github.RelinRan:SerialPort:2023.9.11.6'
}
```
3.CPU架构
```
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a','armeabi-v7a','x86','x86_64'
        }
    }
```
###### 文件依赖
下载的jar放入libs文件夹，so文件放入jniLibs文件夹
```
android {
    sourceSets {
        main {
            jniLibs.srcDirs = ['src/main/jniLibs']
        }
    }
}
```
###### 权限
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
###### 通讯
```
//初始化串口
Serial serial = new Serial("/dev/ttyMSM2",30001,SerialMode.RDWR);
//设置监听
serial.setOnSerialListener(new OnSerialListener() {

    @Override
    public void onSerialSend(byte[] data) {
        //发送内容
    }

    @Override
    public void onSerialReceived(byte[] data) {
        //接收内容
    }
    
});
//打开串口
serial.open();
//关闭串口
serial.close();
```
###### 代理
```
//开启代理（显示通知栏）
SercdService.start(context,"/dev/ttyMSM2","127.0.0.1",30001);
//关闭代理
SercdService.stop(context);
```

