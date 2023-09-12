package android.serial.port.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 代理服务监听
 */
public class SercdReceiver extends BroadcastReceiver {

    private String TAG = SercdReceiver.class.getSimpleName();
    private OnSercdListener onSercdListener;

    public void setOnSercdListener(OnSercdListener onSercdListener) {
        this.onSercdListener = onSercdListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(SercdService.ACTION)) {
            ProxyState state = (ProxyState) intent.getSerializableExtra("state");
            onPendingIntent(state);
        }
    }

    /**
     * 通知意图动作
     *
     * @param state 代理状态
     */
    protected void onPendingIntent(ProxyState state) {
        Log.d(TAG,"sercd state:"+state);
    }

}
