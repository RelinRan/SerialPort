package android.serial.port.api;

/**
 * 串口数据载体
 *
 * @param <T>
 */
public class SerialCarrier<T> {

    /**
     * 发送的数据包
     */
    private SerialPacket<T> send;
    /**
     * 接收的数据
     */
    private byte[] received;
    /**
     * 串口监听
     */
    private OnSerialListener listener;

    /**
     * 构造函数
     *
     * @param send     发送的数据包
     * @param listener 串口监听
     */
    public SerialCarrier(SerialPacket<T> send, OnSerialListener listener) {
        this.send = send;
        this.listener = listener;
    }

    /**
     * 构造函数
     *
     * @param received 接收的数据
     * @param listener 串口监听
     */
    public SerialCarrier(byte[] received, OnSerialListener listener) {
        this.received = received;
        this.listener = listener;
    }

    public byte[] getReceived() {
        return received;
    }

    public void setReceived(byte[] received) {
        this.received = received;
    }

    public OnSerialListener getListener() {
        return listener;
    }

    public void setListener(OnSerialListener listener) {
        this.listener = listener;
    }

    public SerialPacket<T> getSend() {
        return send;
    }

    public void setSend(SerialPacket<T> send) {
        this.send = send;
    }

}
