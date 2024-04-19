package android.serial.port.api;

/**
 * 串口监听
 */
public interface OnSerialListener<T> {

    /**
     * 串口发送
     *
     * @param packet 数据包
     */
    void onSerialSend(SerialPacket<T> packet);

    /**
     * 发送超时
     *
     * @param packet 数据包
     */
    void onSerialTimeout(SerialPacket<T> packet);

    /**
     * 串口接收
     *
     * @param data 接收字节
     */
    void onSerialReceived(byte[] data);

}
