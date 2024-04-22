package android.serial.port.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 串口通讯
 */
public class Serial<T> {
    private Logger logger;
    /**
     * 读取线程
     */
    private ExecutorService service;
    /**
     * 读取线程 - 操作
     */
    private Future readerFuture;
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
    private SerialHandler handler;
    /**
     * 发送队列
     */
    private DelayQueue<SerialPacket<T>> queue;
    /**
     * 发送指令延时，默认50ms
     */
    private long interval = 50;
    /**
     * 超时时间，默认500ms
     */
    private long timeout = 500;
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
    /**
     * 字节工具
     */
    private Bytecode bytecode;
    /**
     * 是否调试模式
     */
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
        this(path, baudRate, mode, bufferSize, new SerialHandler());
    }

    /**
     * 初始化串口
     *
     * @param path       路径
     * @param baudRate   波特率
     * @param mode       模式
     * @param bufferSize 缓存大小
     * @param handler    串口处理
     */
    public Serial(String path, int baudRate, SerialMode mode, int bufferSize, SerialHandler handler) {
        this.path = path;
        this.baudRate = baudRate;
        this.mode = mode.getFlag();
        this.bufferSize = bufferSize;
        this.handler = handler;
        this.service = Executors.newScheduledThreadPool(2);
        logger = Logger.getLogger(Serial.class.getSimpleName());
        bytecode = new Bytecode();
        map = new ConcurrentHashMap<>();
        queue = new DelayQueue<>();
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
        if (handler == null) {
            handler = new SerialHandler();
        }
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
        startWriteService();
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
        if (readCommand == null) {
            readCommand = new ReadCommand();
        }
        readCommand.setBuffer(buffer);
        readerFuture = service.submit(readCommand);
    }

    private ReadCommand readCommand;

    private class ReadCommand implements Runnable {

        private byte[] buffer;

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
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
                            handler.received(data, map.get(key));
                        }
                        long duration = System.currentTimeMillis() - sentTime;
                        if (duration < timeout) {
                            handler.removeTimeout();
                        }
                        received = true;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 开启写入定时任务队列
     */
    private void startWriteService() {
        if (!open) {
            return;
        }
        if (writerFuture != null) {
            writerFuture.cancel(true);
        }
        logger.log(Level.INFO, "start write schedule queue.");
        if (writeCommand == null) {
            writeCommand = new WriteCommand();
        }
        writerFuture = service.submit(writeCommand);
    }

    private WriteCommand writeCommand;

    private class WriteCommand implements Runnable {
        @Override
        public void run() {
            while (open) {
                try {
                    SerialPacket<T> packet = queue.take();
                    write(packet);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 发送
     *
     * @param data 字节数据
     */
    public void send(byte[] data) {
        send(data, null);
    }

    /**
     * 发送
     *
     * @param data  字节数据
     * @param delay 延迟时间
     */
    public void send(byte[] data, long delay) {
        send(data, null, delay);
    }

    /**
     * 发送
     *
     * @param data    字节数据
     * @param options 可选参数
     */
    public void send(byte[] data, T options) {
        send(data, options, interval);
    }

    /**
     * 发送
     *
     * @param data    字节数据
     * @param options 可选参数
     * @param delay   延迟发送时间
     */
    public void send(byte[] data, T options, long delay) {
        if (open) {
            sent = false;
            long time = queue.size() > 0 ? queue.peek().getDelay(TimeUnit.MILLISECONDS) + delay : delay;
            queue.add(new SerialPacket(data, options, time));
        }
    }

    /**
     * 写入内容
     *
     * @param packet 数据包
     */
    private void write(SerialPacket packet) {
        byte[] data = packet.getData();
        if (isOpen()) {
            try {
                if (isDebug()) {
                    logger.log(Level.INFO, "send " + bytecode.toHex(data));
                }
                if (os != null) {
                    os.write(data);
                    for (Long key : map.keySet()) {
                        handler.send(packet, map.get(key));
                        handler.timeout(packet, map.get(key), timeout);
                    }
                    sentTime = System.currentTimeMillis();
                    sent = true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        if (handler != null) {
            handler.removeSend();
            handler.removeReceived();
            handler.removeTimeout();
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

    /**
     * 释放资源
     */
    public void release() {
        close();
        is = null;
        os = null;
        queue = null;
        map = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        handler = null;
        serialPort = null;
        readerFuture = null;
        readCommand = null;
        writerFuture = null;
        writeCommand = null;
    }

}
