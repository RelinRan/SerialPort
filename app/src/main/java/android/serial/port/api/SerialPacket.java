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
    private final byte[] data;
    /**
     * 客户端可选参数
     */
    private final T options;
    /**
     * 执行开始时间
     */
    private final long startTime;

    public SerialPacket(byte[] data) {
        this(data, null, 0);
    }

    public SerialPacket(byte[] data, long delay) {
        this(data, null, delay);
    }

    public SerialPacket(byte[] data, T options, long delay) {
        this.data = data;
        this.options = options;
        this.startTime = System.currentTimeMillis() + delay;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remainingTime = startTime - System.currentTimeMillis();
        return unit.convert(remainingTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    /**
     * 获取数据
     * @return
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 获取可选参数
     * @return
     */
    public T getOptions() {
        return options;
    }
}
