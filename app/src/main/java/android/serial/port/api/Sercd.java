package android.serial.port.api;

import android.content.Context;
import android.content.IntentFilter;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 串口代理
 */
public class Sercd {

    private Context context;
    private SercdReceiver receiver;
    private OnSercdListener onSercdListener;

    public Sercd(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 源网络接口列表
     *
     * @return
     */
    public Map<String, String> feedNetworkInterfacesList() {
        Map<String, String> map = new HashMap<>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface net = nets.nextElement();
                Enumeration<InetAddress> inets = net.getInetAddresses();
                while (inets.hasMoreElements()) {
                    InetAddress inet = inets.nextElement();
                    String name = net.getDisplayName();
                    String address = inet.getHostAddress();
                    map.put(name, address);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 添加状态监听
     *
     * @param onSercdListener
     */
    public void setOnSercdListener(OnSercdListener onSercdListener) {
        this.onSercdListener = onSercdListener;
        register();
    }

    /**
     * 开始代理
     *
     * @param serialPort   串口地址
     * @param netInterface 网络接口
     * @param port         端口
     */
    public void start(String serialPort, String netInterface, int port) {
        File device = new File(serialPort);
        if (!device.exists()) {
            System.err.println(device.getAbsolutePath() + " No such file or directory");
            return;
        }
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    System.err.println(device.getAbsolutePath() + " Failed to modify read and write permissions");
                    return;
                }
                register();
                SercdService.start(context, serialPort, netInterface, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            register();
            SercdService.start(context, serialPort, netInterface, port);
        }
    }

    /**
     * 注册监听
     */
    private void register() {
        if (onSercdListener == null) {
            return;
        }
        if (receiver == null) {
            receiver = new SercdReceiver();
            IntentFilter filter = new IntentFilter(SercdService.ACTION);
            context.registerReceiver(receiver, filter);
        }
        receiver.setOnSercdListener(onSercdListener);
    }

    /**
     * 移除监听
     */
    private void unregister() {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
    }

    /**
     * 停止
     */
    public void stop() {
        unregister();
        SercdService.stop(context);
    }


}
