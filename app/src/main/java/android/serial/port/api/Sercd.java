package android.serial.port.api;

import android.content.Context;

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

    /**
     * 开始代理
     * @param context      上下文
     * @param serialPort   串口地址
     * @param netInterface 网络接口
     * @param port         端口
     */
    public void start(Context context, String serialPort, String netInterface, int port) {
        File device = new File(serialPort);
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    new RuntimeException("device insufficient permissions").printStackTrace();
                    return;
                }
                SercdService.start(context, serialPort, netInterface, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SercdService.start(context, serialPort, netInterface, port);
        }
    }

    /**
     * 停止
     * @param context
     */
    public void stop(Context context) {
        SercdService.stop(context);
    }

    /**
     * 源网络接口列表
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

}
