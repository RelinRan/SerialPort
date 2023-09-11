package android.serial.port.api;

public enum SerialMode {
    /**
     * 只读模式
     */
    RDONLY(00000000),
    /**
     * 只写模式
     */
    WRONLY(00000001),
    /**
     * 读写模式
     */
    RDWR(00000002),
    /**
     * 文件访问模式
     */
    ACC_MODE(00000003);

    private int flag;

    SerialMode(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
