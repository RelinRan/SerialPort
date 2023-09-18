package android.serial.port.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 串口通讯
 */
public class Serial {
    /**
     * 读取线程
     */
    private ExecutorService reader;
    /**
     * 读取线程 - 操作
     */
    private Future readerFuture;
    /**
     * 写入线程
     */
    private ExecutorService writer;
    /**
     * 写入线程 - 操作
     */
    private Future writerFuture;
    /**
     * 是否打开
     */
    private boolean open;
    /**
     * 串口
     */
    private SerialPort serialPort;
    /**
     * 串口路径
     */
    private String path;
    /**
     * 波特率
     */
    private int baudRate;
    /**
     * 模式
     */
    private int mode;
    /**
     * 输入流
     */
    private InputStream is;
    /**
     * 输出流
     */
    private OutputStream os;
    /**
     * 缓存大小
     */
    private int bufferSize;
    /**
     * 串口监听
     */
    private OnSerialListener onSerialListener;
    /**
     * 串口消息通道
     */
    private SerialChannel serialChannel;

    /**
     * 初始化串口，默认缓存64字节，可读写模式
     *
     * @param path     路径
     * @param baudRate 波特率
     */
    public Serial(String path, int baudRate) {
        this(path, baudRate, SerialMode.RDWR, 64);
    }

    /**
     * 初始化串口，默认缓存64字节
     *
     * @param path     路径
     * @param baudRate 波特率
     * @param mode     模式
     */
    public Serial(String path, int baudRate, SerialMode mode) {
        this(path, baudRate, mode, 64);
    }

    /**
     * 初始化串口
     *
     * @param path       路径
     * @param baudRate   波特率
     * @param mode       模式
     * @param bufferSize 缓存大小
     */
    public Serial(String path, int baudRate, SerialMode mode, int bufferSize) {
        this.path = path;
        this.baudRate = baudRate;
        this.mode = mode.getFlag();
        this.bufferSize = bufferSize;
        serialChannel = new SerialChannel();
        this.reader = Executors.newScheduledThreadPool(1);
        this.writer = Executors.newScheduledThreadPool(1);
    }

    /**
     * 初始化串口
     *
     * @param path          路径
     * @param baudRate      波特率
     * @param mode          模式
     * @param bufferSize    缓存大小
     * @param serialChannel 串口通道
     */
    public Serial(String path, int baudRate, SerialMode mode, int bufferSize, SerialChannel serialChannel) {
        this.path = path;
        this.baudRate = baudRate;
        this.mode = mode.getFlag();
        this.bufferSize = bufferSize;
        this.serialChannel = serialChannel;
        this.reader = Executors.newScheduledThreadPool(1);
        this.writer = Executors.newScheduledThreadPool(1);
    }

    /**
     * 设置串口监听
     *
     * @param onSerialListener 串口监听
     */
    public void setOnSerialListener(OnSerialListener onSerialListener) {
        this.onSerialListener = onSerialListener;
    }

    /**
     * 打开串口
     */
    public void open() {
        readerFuture = reader.submit(() -> {
            serialPort = new SerialPort(new File(path), baudRate, mode);
            open = serialPort.isOpen();
            is = serialPort.getInputStream();
            os = serialPort.getOutputStream();
            while (open) {
                int size;
                try {
                    byte[] buffer = new byte[bufferSize];
                    if (is == null) {
                        return;
                    }
                    size = is.read(buffer);
                    if (size > 0 && onSerialListener != null) {
                        byte[] data = Arrays.copyOfRange(buffer, 0, size);
                        serialChannel.received(data, onSerialListener);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发送
     *
     * @param data 字节数据
     */
    public synchronized void send(byte[] data) {
        writerFuture = writer.submit(() -> {
            try {
                os.write(data);
                if (onSerialListener != null) {
                    serialChannel.send(data, onSerialListener);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 关闭串口操作
     */
    public void close() {
        open = false;
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serialChannel != null) {
            serialChannel.removeCallbacksAndMessages(null);
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        if (readerFuture != null) {
            readerFuture.cancel(true);
        }
        if (writerFuture != null) {
            writerFuture.cancel(true);
        }
    }

}
