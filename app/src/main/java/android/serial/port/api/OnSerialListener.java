package android.serial.port.api;

/**
 * 串口监听
 */
public interface OnSerialListener {

    /**
     * 串口发送
     *
     * @param data 发送字节
     */
    void onSerialSend(byte[] data);

    /**
     * 发送超时
     * @param data
     */
    void onSerialTimeout(byte[] data);

    /**
     * 串口接收
     *
     * @param data 接收字节
     */
    void onSerialReceived(byte[] data);

}
