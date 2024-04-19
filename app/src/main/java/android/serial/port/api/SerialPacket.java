package android.serial.port.api;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Packet<T> implements Delayed {

    private final byte[] data;
    private final Object obj;
    private final long startTime;

    public Packet(byte[] data, long delay) {
        this(data,null,delay);
    }

    public Packet(byte[] data, Object obj, long delay) {
        this.data = data;
        this.obj = obj;
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

    public byte[] getData() {
        return data;
    }

    public Object getObj() {
        return obj;
    }

}
