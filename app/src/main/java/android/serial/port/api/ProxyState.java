package android.serial.port.api;

/**
 * 代理状态
 */
public enum ProxyState {
    /**
     * 就绪
     */
    STATE_READY,
    /**
     * 已连接
     */
    STATE_CONNECTED,
    /**
     * 串口已打开
     */
    STATE_PORT_OPENED,
    /**
     * 已停止
     */
    STATE_STOPPED,
    /**
     * 异常停止
     */
    STATE_CRASHED
}