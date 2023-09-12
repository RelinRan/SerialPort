package android.serial.port.api;

/**
 * 代理监听
 */
public interface OnSercdListener {

    /**
     * 代理状态监听
     * @param state 状态
     */
    void onSercdStateChange(ProxyState state);

}
