package android.serial.port.api;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


public class SerialChannel extends Handler {

    public void send(byte[] data, OnSerialListener listener) {
        Message message = obtainMessage();
        message.what = 1;
        message.obj = listener;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        message.setData(bundle);
        sendMessage(message);
    }

    public void received(byte[] data, OnSerialListener listener) {
        Message message = obtainMessage();
        message.what = 2;
        message.obj = listener;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        message.setData(bundle);
        sendMessage(message);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int what = msg.what;
        byte[] data = msg.getData().getByteArray("data");
        OnSerialListener listener = (OnSerialListener) msg.obj;
        if (what == 1) {
            listener.onSerialSend(data);
        }
        if (what == 2) {
            listener.onSerialReceived(data);
        }
    }

}
