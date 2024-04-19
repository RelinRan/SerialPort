package android.serial.port.api;

public class SerialParams<T> {

    private SerialPacket<T> packet;
    private byte[] data;
    private OnSerialListener listener;

    public SerialParams(SerialPacket<T> packet, OnSerialListener listener) {
        this.packet = packet;
        this.listener = listener;
    }

    public SerialParams(byte[] data, OnSerialListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public OnSerialListener getListener() {
        return listener;
    }

    public void setListener(OnSerialListener listener) {
        this.listener = listener;
    }

    public SerialPacket<T> getPacket() {
        return packet;
    }

    public void setPacket(SerialPacket<T> packet) {
        this.packet = packet;
    }
}
