package android.serial.port.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 串口通讯
 */
public class Serial {
    private Logger logger;
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
    private SerialChannel channel;
    /**
     * 发送队列
     */
    private ConcurrentLinkedQueue<byte[]> queue;
    /**
     * 发送第一次延时,默认0
     */
    private long firstInterval = 0;
    /**
     * 发送指令延时，默认50ms
     */
    private long interval = 50;
    /**
     * 超时时间，默认500ms
     */
    private long timeout = 500;
    /**
     * 延时单位，默认毫秒
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    /**
     * 是否已收到数据
     */
    private boolean received;
    /**
     * 是否已发送完毕
     */
    private boolean sent;
    /**
     * 发送指令时间
     */
    private long sentTime = 0;
    private Bytecode bytecode;
    private boolean debug;

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
     * @param path       路径
     * @param baudRate   波特率
     * @param mode       模式
     * @param bufferSize 缓存大小
     * @param channel    串口通道
     */
    public Serial(String path, int baudRate, SerialMode mode, int bufferSize, SerialChannel channel) {
        this.path = path;
        this.baudRate = baudRate;
        this.mode = mode.getFlag();
        this.bufferSize = bufferSize;
        this.channel = channel;
        this.reader = Executors.newCachedThreadPool();
        this.writer = Executors.newScheduledThreadPool(1);
        logger = Logger.getLogger(Serial.class.getSimpleName());
        bytecode = new Bytecode();
        map = new ConcurrentHashMap<>();
        queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 是否调试模式
     *
     * @return
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置调试模式
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 设置超时时间
     *
     * @param timeout 默认单位ms
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
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
     * @param interval
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * 设置第一次发送延迟时间
     *
     * @param interval
     */
    public void setFirstInterval(long interval) {
        this.firstInterval = interval;
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
    public boolean isReceived() {
        return received;
    }

    /**
     * 是否已写完成
     *
     * @return
     */
    public boolean isSent() {
        return sent;
    }

    /**
     * 打开串口
     */
    public void open() {
        if (readerFuture != null) {
            readerFuture.cancel(true);
        }
        if (writerFuture != null) {
            writerFuture.cancel(true);
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        serialPort = new SerialPort(new File(path), baudRate, mode);
        open = serialPort.isOpen();
        startReadService();
        startWriteScheduleQueue();
    }

    /**
     * 开启读取服务
     */
    private void startReadService() {
        if (!open) {
            return;
        }
        byte[] buffer = new byte[bufferSize];
        is = serialPort.getInputStream();
        os = serialPort.getOutputStream();
        if (is == null) {
            return;
        }
        readerFuture = reader.submit(() -> {
            logger.log(Level.INFO, "start read service.");
            while (open) {
                try {
                    if (is == null) {
                        break;
                    }
                    int length = is.read(buffer);
                    if (length > 0) {
                        received = false;
                        for (Long key : map.keySet()) {
                            byte[] data = Arrays.copyOfRange(buffer, 0, length);
                            if (isDebug()) {
                                logger.log(Level.INFO, "received " + bytecode.toHex(data));
                            }
                            channel.received(data, map.get(key));
                        }
                        long duration = System.currentTimeMillis() - sentTime;
                        if (duration < timeout) {
                            channel.removeTimeout();
                        }
                        received = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 开启写入定时任务队列
     */
    private void startWriteScheduleQueue() {
        if (!open) {
            return;
        }
        if (writerFuture != null) {
            writerFuture.cancel(true);
        }
        logger.log(Level.INFO, "start write schedule queue.");
        writerFuture = writer.scheduleAtFixedRate(() -> {
            if (!queue.isEmpty()) {
                write(queue.poll());
            }
        }, firstInterval, interval, timeUnit);
    }

    /**
     * 写入内容
     *
     * @param data
     */
    private void write(byte[] data) {
        if (isOpen()) {
            try {
                if (isDebug()) {
                    logger.log(Level.INFO, "send " + bytecode.toHex(data));
                }
                if (os != null) {
                    os.write(data);
                    for (Long key : map.keySet()) {
                        channel.send(data, map.get(key));
                        channel.timeout(data, map.get(key), timeout);
                    }
                    sentTime = System.currentTimeMillis();
                    sent = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送
     *
     * @param data 字节数据
     */
    public void send(byte[] data) {
        if (open) {
            sent = false;
            queue.offer(data);
        }
    }

    /**
     * 关闭串口操作
     */
    public void close() {
        logger.log(Level.INFO, "close");
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
        if (channel != null) {
            channel.removeCallbacksAndMessages(null);
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
