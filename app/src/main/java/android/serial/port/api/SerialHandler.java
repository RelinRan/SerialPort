package android.serial.port.api;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


/***
 * 串口通信通道
 */
public class SerialChannel extends Handler {

    /**
     * 发送
     *
     * @param data     数据
     * @param listener 监听
     */
    public void send(Packet packet, OnSerialListener listener) {
        Message message = obtainMessage();
        message.what = 1;
        message.obj = listener;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", packet);
        message.setData(bundle);
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
        message.what = 2;
        message.obj = listener;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        message.setData(bundle);
        sendMessage(message);
    }

    /**
     * 超时
     *
     * @param data     数据
     * @param listener 监听
     * @param delay    延迟时间，单位ms
     */
    public void timeout(byte[] data, OnSerialListener listener, long delay) {
        Message message = obtainMessage();
        message.what = 3;
        message.obj = listener;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        message.setData(bundle);
        sendMessageDelayed(message, delay);
    }

    /**
     * 移除超时
     */
    public void removeTimeout() {
        removeMessages(3);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int what = msg.what;
        if (msg.getData() == null || msg.obj == null) {
            return;
        }
        byte[] data = msg.getData().getByteArray("data");
        OnSerialListener listener = (OnSerialListener) msg.obj;
        if (what == 1) {
            listener.onSerialSend(data);
        } else if (what == 2) {
            listener.onSerialReceived(data);
        } else if (what == 3) {
            listener.onSerialTimeout(data);
        }
    }

}
