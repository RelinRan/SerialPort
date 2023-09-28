/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.serial.port.api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 串口
 */
public class SerialPort {

    private FileDescriptor fd;
    private FileInputStream fis;
    private FileOutputStream fos;
    private boolean open;
    private Logger logger;

    /**
     * 是否能读写
     *
     * @param device 设备串口
     * @return
     */
    public boolean isCanReadWrite(File device) {
        String path = device.getAbsolutePath();
        if (!device.exists()) {
            logger.log(Level.SEVERE, path + " serial port is not exist.");
            return false;
        }
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su = Runtime.getRuntime().exec("/system/bin/su");
                StringBuilder cmd = new StringBuilder();
                cmd.append("chmod 666 ").append(device.getAbsolutePath()).append("\nexit\n");
                su.getOutputStream().write(cmd.toString().getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    logger.log(Level.SEVERE, path + " serial port failed to modify permissions");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化串口
     *
     * @param device   串口文件
     * @param baudRate 波特率
     * @param mode     标识 0：只读模式，1：只写模式，2：读写模式，3：文件访问模式
     */
    public SerialPort(File device, int baudRate, int mode) {
        logger = Logger.getLogger(SerialPort.class.getSimpleName());
        String path = device.getAbsolutePath();
        if (isCanReadWrite(device)) {
            fd = open(path, baudRate, mode);
            if (fd != null) {
                open = true;
                fis = new FileInputStream(fd);
                fos = new FileOutputStream(fd);
                logger.log(Level.INFO, path + " " + baudRate + " " + mode + " " + open);
            } else {
                open = false;
                logger.log(Level.SEVERE, "open failed");
            }
        }
    }

    /**
     * 是否打开串口
     *
     * @return
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * 获取输入流
     *
     * @return
     */
    public InputStream getInputStream() {
        return fis;
    }

    /**
     * 获取输出流
     *
     * @return
     */
    public OutputStream getOutputStream() {
        return fos;
    }

    /**
     * 打开串口
     *
     * @param path     路径
     * @param baudRate 波特率
     * @param flags    标识
     * @return
     */
    private native static FileDescriptor open(String path, int baudRate, int flags);

    /**
     * 关闭串口
     */
    public native void close();

    static {
        System.loadLibrary("serial");
    }
}
