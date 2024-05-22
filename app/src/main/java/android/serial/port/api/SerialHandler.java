package android.serial.port.api;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


/***
 * 串口通信通道
 */
public class SerialHandler extends Handler {

    /**
     * 发送
     */
    private final int SEND = 1;
    /**
     * 接收
     */
    private final int RECEIVED = 2;
    /**
     * 超时
     */
    private final int TIMEOUT = 3;

    public SerialHandler(Looper looper) {
        super(looper);
    }

    /**
     * 发送
     *
     * @param packet   数据包
     * @param listener 监听
     */
    public void send(SerialPacket packet, OnSerialListener listener) {
        Message message = obtainMessage();
        message.what = SEND;
        message.obj = new SerialCarrier(packet, listener);
        sendMessage(message);
    }

    /**
     * 接收信息
     *
     * @param data     数据
     * @param listener 监听
     */
    public void received(byte[] data, OnSerialListener listener) {
        Message message = obtainMessage();
        message.what = RECEIVED;
        message.obj = new SerialCarrier(data, listener);
        sendMessage(message);
    }

    /**
     * 超时
     *
     * @param packet   数据包
     * @param listener 监听
     * @param delay    延迟时间，单位ms
     */
    public void timeout(SerialPacket packet, OnSerialListener listener, long delay) {
        Message message = obtainMessage();
        message.what = TIMEOUT;
        message.obj = new SerialCarrier(packet, listener);
        sendMessageDelayed(message, delay);
    }

    /**
     * 移除发送信息
     */
    public void removeSend() {
        removeMessages(SEND);
    }

    /**
     * 移除接收信息
     */
    public void removeReceived() {
        removeMessages(RECEIVED);
    }

    /**
     * 移除超时
     */
    public void removeTimeout() {
        removeMessages(TIMEOUT);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.obj == null) {
            return;
        }
        SerialCarrier carrier = (SerialCarrier) msg.obj;
        OnSerialListener listener = carrier.getListener();
        if (listener == null) {
            return;
        }
        switch (msg.what) {
            case SEND:
                listener.onSerialSend(carrier.getSend());
                break;
            case RECEIVED:
                listener.onSerialReceived(carrier.getReceived());
                break;
            case TIMEOUT:
                listener.onSerialTimeout(carrier.getSend());
                break;
        }
    }

}
