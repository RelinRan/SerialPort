##### Serial Port

Android 串口通讯 arm64-v8a、armeabi-v7a、x86、x86_64

##### AAR

|名称|操作|
|-|-|
|serial.jar|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/serial-2024.3.21.1.jar)|
|arm64-v8a|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/arm64-v8a/libserial.so)|
|armeabi-v7a|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/armeabi-v7a/libserial.so)|
|x86|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/x86/libserial.so)|
|x86_64|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/x86_64/libserial.so)|
|arm-zip|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/arm.zip)|
|x86-zip|[下载](https://github.com/RelinRan/SerialPort/blob/main/libs/x86.zip)|

##### Maven

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
	implementation 'com.github.RelinRan:SerialPort:2024.3.21.1'
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

##### 文件依赖

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

##### 权限

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

##### 路径

所有驱动路径

```
 SerialPortFinder finder = new SerialPortFinder();
 String[] paths = finder.getAllDevicesPath();
 for (String path : paths) {
     Log.i("SerialPortFinder","path:"+path);
 }
```

##### 通讯

初始化串口

```
Serial serial = new Serial("/dev/ttyMSM2",30001,SerialMode.RDWR);
serial.setDebug(true);
```


添加监听

```
long sid = serial.addSerialListener(new OnSerialListener() {

    @Override
    public void onSerialSend(byte[] data) {
        //发送内容
    }

    @Override
    public void onSerialReceived(byte[] data) {
        //接收内容
    }
    
});
```
移除监听

```
serial.remove(sid);
```
打开串口

```
serial.open();
```
关闭串口

```
serial.close();
```

##### 代理

服务配置

```
<service
    android:name="android.serial.port.api.SercdService"
    android:directBootAware="true"
    android:enabled="true" />
```

初始化

```
Sercd sercd = new Sercd(getContext());
```

网络接口列表 + 端口

```
String netInterface = "";
int port = 30001;
Map<String, String> map = sercd.feedNetworkInterfacesList();
for (String key:map.keySet()){
    //wifi是wlan0
    if (key.equals("eth0")){
        netInterface = map.get(key);
    }
}
```

设置监听

```
sercd.setOnSercdListener(new OnSercdListener() {
    @Override
    public void onSercdStateChange(ProxyState proxyState) {
        System.out.println("proxyState:"+proxyState);
    }
});
```

开始代理

```
sercd.start("/dev/ttyMSM2",netInterface,port);
```

关闭代理

```
sercd.stop();
```
##### 字节工具
```
Bytecode bytecode = new Bytecode();
byte b = 0b00001111;
```
Boolean值Byte
```
byte b = bytecode.toByte(false,false,false,false,true,true,true,true);//00001111
```
Byte的Boolean[]
```
boolean[] booleans = bytecode.toBooleans(b);//[false, false, false, false, true, true, true, true]
```
Byte转十六进制
```
String hex = bytecode.toHex(b);//0F
```
Byte转十进制
```
int dec = bytecode.toDec(b);//15
```
Byte转八进制
```
String oct = bytecode.toOct(b);//017
```
Byte转二进制
```
String bin = bytecode.toBin(b);//00001111
```
Byte数组转十六进制字符串
```
float floatValue = 99.99f;
byte[] data = bytecode.toBytes(floatValue);
String hexString = bytecode.toHex(data);//E1 FA C7 42 
```
十六进制String转Byte数组
```
byte[] value = bytecode.toBytes(hexString);//[-31, -6, -57, 66]
```
Byte数组转Float
```
float floatValue = 99.99f;
byte[] data = bytecode.toBytes(floatValue);
float value = bytecode.toFloat(new byte[]{data[0],data[1],data[2],data[3]}));//99.99
```
Byte数组转Short
```
short shortValue = 33;
byte[] data = bytecode.toBytes(shortValue);
short value = bytecode.toShort(new byte[]{data[0],data[1]}));//33
```
Byte数组转Int
```
int intValue = 100;
byte[] data = bytecode.toBytes(intValue);
int value = bytecode.toInt(new byte[]{data[0],data[1],data[2],data[3]}));//100
```



