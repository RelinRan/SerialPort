package android.serial.port.api;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 字节码操作
 */
public class Bytecode {

    /**
     * byte转16进制字符串
     *
     * @param value 字节
     * @return
     */
    public String toHex(byte value) {
        return String.format("%02X", value);
    }

    /**
     * byte转十进制字符串
     *
     * @param value byte
     * @return
     */
    public int toDec(byte value) {
        return value;
    }

    /**
     * byte转八进制字符串
     *
     * @param value 字节
     * @return
     */
    public String toOct(byte value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i >= 0; i--) {
            int octalDigit = (value >> (i * 3)) & 0b111;
            builder.append(octalDigit);
        }
        return builder.toString();
    }

    /**
     * byte转二进制字符串
     *
     * @param value 字节
     * @return
     */
    public String toBin(byte value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            boolean bit = ((value >> (7 - i)) & 0x01) != 0;
            builder.append(bit ? 1 : 0);
        }
        return builder.toString();
    }

    /**
     * 将进制字符串转换为字节
     *
     * @param radix 基数,例如16：十六进制，8：八进制
     * @param value 对应字符
     * @return
     */
    public byte toByte(int radix, String value) {
        return (byte) Integer.parseInt(value, radix);
    }

    /**
     * 字节转16进制字符串
     *
     * @param data 字节数组
     * @return
     */
    public String toHex(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte bt : data) {
            String hex = String.format("%02X", bt);//按照两位十六进制格式化
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
     * int转1个字节
     *
     * @param value 值
     * @return
     */
    public byte toByte(int value) {
        return (byte) value;
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
     * float转4个字节数组
     *
     * @param value 整数
     * @return
     */
    public byte[] toBytes(float value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    /**
     * 字节转4个字节的float
     *
     * @param data 字节
     * @return
     */
    public float toFloat(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
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
     * 数字字符转short
     *
     * @param value    数字字符
     * @param multiple 放大倍数
     * @return
     */
    public short toShort(String value, int multiple) {
        if (value.contains(".")) {
            return (short) (Float.parseFloat(value) * multiple);
        }
        return (short) (Short.parseShort(value) * multiple);
    }

    /**
     * 数字字符转1个字节
     *
     * @param value    数字字符
     * @param multiple 放大倍数
     * @return
     */
    public byte toByte(String value, int multiple) {
        return toByte(toShort(value, multiple));
    }

    /**
     * 数字字符转2个字节
     *
     * @param value    数字字符
     * @param multiple 放大倍数
     * @return
     */
    public byte[] toBytes(String value, int multiple) {
        return toBytes(toShort(value, multiple));
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
     * short转2个字节数组
     *
     * @param value 整数
     * @return
     */
    public byte[] toBytes(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    /**
     * 字节转2个字节的short
     *
     * @param data 字节
     * @return
     */
    public short toShort(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
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
     * int转4个字节数组
     *
     * @param value 整数
     * @return
     */
    public byte[] toBytes(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    /**
     * 字节转4个字节的int
     *
     * @param data 字节
     * @return
     */
    public int toInt(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
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

    /**
     * double转8个字节数组
     *
     * @param value 整数
     * @return
     */
    public byte[] toBytes(double value) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(value).array();
    }

    /**
     * 字节转8个字节的double
     *
     * @param data 字节
     * @return
     */
    public double toDouble(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getDouble();
    }

    /**
     * byte每个bit是否是1,例如:0b01100101 -> [true, false, true, false, false, true, true, false]
     *
     * @param values
     * @return
     */
    public byte toByte(boolean... values) {
        int length = values.length;
        int len = length > 8 ? 8 : length;
        byte value = 0;
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                if (values[i]) {
                    value |= (1 << (7 - i));
                }
            }
        }
        return value;
    }

    /**
     * byte每个bit是否是1,例如:0b01100101 -> [true, false, true, false, false, true, true, false]
     *
     * @param value byte
     * @return
     */
    public boolean[] toBooleans(byte value) {
        boolean[] bits = new boolean[8];
        for (int i = 0; i < 8; i++) {
            bits[i] = ((value >> (7 - i)) & 0x01) != 0;
        }
        return bits;
    }

    /**
     * byte每个bit是否是1,例如:0b01100101 -> [true, false, true, false, false, true, true, false]
     *
     * @param value byte
     * @param bits  数组结果，例如:0b01100101 -> [true, false, true, false, false, true, true, false]
     */
    public void toBooleans(byte value, boolean[] bits) {
        if (bits == null) {
            return;
        }
        int length = bits.length;
        length = length > 8 ? 8 : length;
        if (length == 0) {
            return;
        }
        for (int i = 0; i < length; i++) {
            bits[i] = ((value >> (7 - i)) & 0x01) != 0;
        }
    }

    /**
     * 字节数字总和的结果字节
     *
     * @param data 字节
     * @return
     */
    public byte sum(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i] & 0xFF;
            sum &= 0xFF;//溢出则循环计数
        }
        return (byte) sum;
    }

    /**
     * 字节数字总和的结果字节
     *
     * @param data   字节
     * @param length 长度
     * @return
     */
    public byte sum(byte[] data, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += data[i] & 0xFF;
            sum &= 0xFF;//溢出则循环计数
        }
        return (byte) sum;
    }

    /**
     * 字节数字总和的结果字节
     *
     * @param data  字节
     * @param start 开始位置
     * @param end   结束位置
     * @return
     */
    public byte sum(byte[] data, int start, int end) {
        int sum = 0;
        for (int i = start; i <= end; i++) {
            sum += data[i] & 0xFF;
            sum &= 0xFF;//溢出则循环计数
        }
        return (byte) sum;
    }

}
