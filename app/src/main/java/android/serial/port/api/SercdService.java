package android.serial.port.api;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * 代理服务
 */
public class SercdService extends Service {
    /**
     * 串口地址
     */
    private static final String SERIAL_PORT = "serial_port";
    /**
     * 接口
     */
    private static final String INTERFACE = "interface";
    /**
     * 端口
     */
    private static final String PORT = "port";
    /**
     * 代理动作
     */
    public static final String ACTION = "android.serial.port.proxy";

    /**
     * 开始代理
     *
     * @param context      上下文
     * @param serialPort   串口地址
     * @param netInterface 网络接口
     * @param port         端口
     */
    public static void start(Context context, String serialPort, String netInterface, int port) {
        Intent myself = new Intent(context, SercdService.class);
        myself.putExtra(SERIAL_PORT, serialPort);
        myself.putExtra(INTERFACE, netInterface);
        myself.putExtra(PORT, port);
        context.startService(myself);
    }

    /**
     * 停止代理
     *
     * @param context 上下文
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, SercdService.class);
        context.stopService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ProxyState mState;
    private String mSerialPort;
    private String mInterface;
    private int mPort;
    private NotificationManager mNotificationManager;
    private Context mContext;
    private PendingIntent mContentIntent;
    private Intent intent;
    private boolean mExiting;
    private SercdReceiver receiver;

    private Thread mSercdThread = new Thread() {
        @Override
        public void run() {
            main(mSerialPort, mInterface, mPort);
            if (mExiting) {
                changeState(ProxyState.STATE_STOPPED);
            } else {
                changeState(ProxyState.STATE_CRASHED);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("serial");
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        receiver = new SercdReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        registerReceiver(receiver, filter);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        intent = new Intent(this, SercdReceiver.class);
        mContentIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mContext = getApplicationContext();
        mState = ProxyState.STATE_STOPPED;
        mExiting = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSerialPort = intent.getStringExtra(SERIAL_PORT);
        mInterface = intent.getStringExtra(INTERFACE);
        mPort = intent.getIntExtra(PORT, 0);
        mSercdThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (mSercdThread.isAlive()) {
            mExiting = true;
            exit();
            try {
                mSercdThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        changeState(ProxyState.STATE_STOPPED);
        super.onDestroy();
    }

    /**
     * This method is called from libsercd.so
     *
     * @param newstate New sercd state.
     */
    private synchronized void changeState(ProxyState newstate) {
        if (newstate == mState) {
            return;
        }
        /* Remove old notification if necessary */
        if (mState != ProxyState.STATE_STOPPED) {
            mNotificationManager.cancelAll();
        }
        /* Create new notification */
        long when = System.currentTimeMillis();
        CharSequence contentTitle = getText(R.string.app_name);
        int icon;
        CharSequence text;
        switch (newstate) {
            case STATE_READY:
                icon = R.drawable.notification_icon_ready;
                text = getText(R.string.notif_ready);
                break;
            case STATE_CONNECTED:
                icon = R.drawable.notification_icon_ready;
                text = getText(R.string.notif_connected);
                break;
            case STATE_PORT_OPENED:
                icon = R.drawable.notification_icon_connected;
                text = getText(R.string.notif_opened);
                break;
            case STATE_CRASHED:
                icon = R.drawable.notification_icon_ready;
                text = getText(R.string.notif_crash);
            default:
                return;
        }
        intent.putExtra("state", newstate);
        intent.putExtra("icon", icon);
        intent.putExtra("text", text);
        Notification.Builder builder = new Notification.Builder(mContext)
                .setSmallIcon(icon)
                .setContentTitle(contentTitle)
                .setContentText(text)
                .setContentIntent(mContentIntent)
                .setOngoing(true)
                .setAutoCancel(false);
        Notification notification = builder.build();
        //将通知的标志位设置为持续显示和禁止滑动清除的组合
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
        mState = newstate;
    }

    private native int main(String serialPort, String netInterface, int port);

    private native void exit();
}
