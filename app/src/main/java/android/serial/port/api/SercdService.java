package android.serial.port.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 代理服务
 */
public class SercdService extends Service {

    private String TAG = SercdService.class.getSimpleName();
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
    private Intent intent;
    private boolean mExiting;

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
        intent = new Intent(ACTION);
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
        mState = newstate;
        Log.d(TAG, "change state:" + newstate);
        intent.putExtra("state", newstate);
        sendBroadcast(intent);
    }

    private native int main(String serialPort, String netInterface, int port);

    private native void exit();
}
