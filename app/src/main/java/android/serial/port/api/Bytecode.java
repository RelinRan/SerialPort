package android.serial.port.api;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 字节码操作
 */
public class Bytecode {

    /**
     * 字节转16进制字符串
     *
     * @param data 字节数组
     * @return
     */
    public String toHex(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            String hex = String.format("%02X", b);//按照两位十六进制格式化
            builder.append(hex).append(" ");
        }
        return builder.toString().toUpperCase();
    }

    /**
     * 16进制字符串转字符串
     *
     * @param hexString 16进制字符
     * @return
     */
    public byte[] toBytes(String hexString) {
        hexString = hexString.replace(" ", "");
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }
        return bytes;
    }

    /**
     * float转4个字节数组
     *
     * @param value 整数
     * @param order 字节顺序
     * @return
     */
    public byte[] toBytes(float value, ByteOrder order) {
        return ByteBuffer.allocate(4).order(order).putFloat(value).array();
    }

    /**
     * 字节转4个字节的float
     *
     * @param data  字节
     * @param order 字节顺序
     * @return
     */
    public float toFloat(byte[] data, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(order);
        return buffer.getFloat();
    }

    /**
     * short转2个字节数组
     *
     * @param value 整数
     * @param order 字节顺序
     * @return
     */
    public byte[] toBytes(short value, ByteOrder order) {
        return ByteBuffer.allocate(2).order(order).putShort(value).array();
    }

    /**
     * 字节转2个字节的short
     *
     * @param data  字节
     * @param order 字节顺序
     * @return
     */
    public short toShort(byte[] data, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(order);
        return buffer.getShort();
    }

    /**
     * int转4个字节数组
     *
     * @param value 整数
     * @param order 字节顺序
     * @return
     */
    public byte[] toBytes(int value, ByteOrder order) {
        return ByteBuffer.allocate(4).order(order).putInt(value).array();
    }

    /**
     * 字节转4个字节的int
     *
     * @param data  字节
     * @param order 字节顺序
     * @return
     */
    public int toInt(byte[] data, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(order);
        return buffer.getInt();
    }

    /**
     * double转8个字节数组
     *
     * @param value 整数
     * @param order 字节顺序
     * @return
     */
    public byte[] toBytes(double value, ByteOrder order) {
        return ByteBuffer.allocate(8).order(order).putDouble(value).array();
    }

    /**
     * 字节转8个字节的double
     *
     * @param data  字节
     * @param order 字节顺序
     * @return
     */
    public double toDouble(byte[] data, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(order);
        return buffer.getDouble();
    }
    
}
