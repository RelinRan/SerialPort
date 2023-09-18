package android.serial.port.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private ScheduledExecutorService writer;
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
    private ConcurrentHashMap<Long, OnSerialListener> map;
    /**
     * 串口消息通道
     */
    private SerialChannel serialChannel;
    /**
     * 发送队列
     */
    private ConcurrentLinkedQueue<byte[]> queue;
    /**
     * 发送第一次延时,默认0
     */
    private long initialDelay = 0;
    /**
     * 发送指令延时，默认50ms
     */
    private long period = 50;
    /**
     * 延时单位，默认毫秒
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private boolean read;
    private boolean written;

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
        this(path, baudRate, mode, bufferSize, new SerialChannel());
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
        this.reader = Executors.newCachedThreadPool();
        this.writer = Executors.newScheduledThreadPool(1);
        map = new ConcurrentHashMap<>();
        queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 设置串口监听
     *
     * @param onSerialListener
     * @return 监听id
     */
    public long addSerialListener(OnSerialListener onSerialListener) {
        long sid = System.currentTimeMillis();
        map.put(sid, onSerialListener);
        return sid;
    }

    /**
     * 删除监听
     *
     * @param ids
     */
    public void remove(long... ids) {
        for (long id : ids) {
            map.remove(id);
        }
    }

    /**
     * 设置发送间隔时间
     *
     * @param period
     */
    public void setPeriod(long period) {
        this.period = period;
    }

    /**
     * 设置第一次发送延迟时间
     *
     * @param initialDelay
     */
    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    /**
     * 设置时间单位
     *
     * @param timeUnit
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
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
     * 是否已读
     *
     * @return
     */
    public boolean isRead() {
        return read;
    }

    /**
     * 是否已写完成
     *
     * @return
     */
    public boolean isWritten() {
        return written;
    }

    /**
     * 打开串口
     */
    public void open() {
        if (readerFuture != null) {
            readerFuture.cancel(true);
        }
        serialPort = new SerialPort(new File(path), baudRate, mode);
        open = serialPort.isOpen();
        if (open) {
            waitRead();
            waitWrite();
        }
    }

    /**
     * 等待读取
     */
    private void waitRead() {
        readerFuture = reader.submit(() -> {
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
                    if (size > 0) {
                        read = false;
                        for (Long key : map.keySet()) {
                            byte[] data = Arrays.copyOfRange(buffer, 0, size);
                            serialChannel.received(data, map.get(key));
                        }
                        read = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 等待发送
     */
    private void waitWrite() {
        if (writerFuture != null) {
            writerFuture.cancel(true);
        }
        writerFuture = writer.scheduleAtFixedRate(() -> {
            while (open) {
                if (!queue.isEmpty()) {
                    write(queue.poll());
                }
            }
        }, initialDelay, period, timeUnit);
    }

    /**
     * 写入内容
     *
     * @param data
     */
    private void write(byte[] data) {
        try {
            written = false;
            os.write(data);
            for (Long key : map.keySet()) {
                serialChannel.send(data, map.get(key));
            }
            written = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送
     *
     * @param data 字节数据
     */
    public void send(byte[] data) {
        queue.offer(data);
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
        if (!queue.isEmpty()) {
            queue.clear();
        }
        if (!map.isEmpty()) {
            map.clear();
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
