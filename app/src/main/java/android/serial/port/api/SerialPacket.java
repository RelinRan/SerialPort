package android.serial.port.api;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 串口数据包
 *
 * @param <T>
 */
public class SerialPacket<T> implements Delayed {

    /**
     * 串口数据
     */
    private byte[] data;
    /**
     * 客户端可选参数
     */
    private T options;
    /**
     * 执行开始时间
     */
    private long startTime;
    /**
     * 消息id
     */
    private String id;

    /**
     * 构造
     *
     * @param id   编号
     * @param data 数据
     */
    public SerialPacket(String id, byte[] data) {
        this(id, data, null, 0);
    }

    /**
     * 构造
     *
     * @param id    编号
     * @param data  数据
     * @param delay 延迟
     */
    public SerialPacket(String id, byte[] data, long delay) {
        this(id, data, null, delay);
    }

    /**
     * 构造
     *
     * @param id      编号
     * @param data    数据
     * @param options 参数
     * @param delay   延迟
     */
    public SerialPacket(String id, byte[] data, T options, long delay) {
        this.id = id;
        this.data = data;
        this.options = options;
        this.startTime = System.currentTimeMillis() + delay;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remainingTime = startTime - System.currentTimeMillis();
        return unit.convert(remainingTime, unit);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    /**
     * 获取数据
     *
     * @return
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 获取可选参数
     *
     * @return
     */
    public T getOptions() {
        return options;
    }

    /**
     * 获取数据包id
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 释放资源
     */
    public void release() {
        data = null;
        options = null;
        id = null;
    }
}
