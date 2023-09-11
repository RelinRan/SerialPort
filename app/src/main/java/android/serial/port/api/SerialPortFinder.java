/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.serial.port.api;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

/**
 * 串口查找器
 */
public class SerialPortFinder {
    private static final String TAG = SerialPortFinder.class.getSimpleName();

    private Vector<Driver> mDrivers = null;

    /**
     * 获取驱动
     *
     * @return
     */
    public Vector<Driver> getDrivers() {
        if (mDrivers == null) {
            mDrivers = new Vector<>();
            try {
                LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
                String l;
                while ((l = r.readLine()) != null) {
                    // Issue 3:
                    // Since driver name may contain spaces, we do not extract driver name with split()
                    String drivername = l.substring(0, 0x15).trim();
                    String[] w = l.split(" +");
                    if ((w.length >= 5) && (w[w.length - 1].equals("serial"))) {
                        Log.d(TAG, "Found new driver " + drivername + " on " + w[w.length - 4]);
                        mDrivers.add(new Driver(drivername, w[w.length - 4]));
                    }
                }
                r.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mDrivers;
    }

    /**
     * 获取所有驱动
     *
     * @return
     */
    public String[] getAllDevices() {
        Vector<String> devices = new Vector<String>();
        // Parse each driver
        Iterator<Driver> itdriv = getDrivers().iterator();
        while (itdriv.hasNext()) {
            Driver driver = itdriv.next();
            Iterator<File> itdev = driver.getDevices().iterator();
            while (itdev.hasNext()) {
                String device = itdev.next().getName();
                String value = String.format("%s (%s)", device, driver.getName());
                devices.add(value);
            }
        }
        return devices.toArray(new String[devices.size()]);
    }

    /**
     * 获取所有驱动路径
     *
     * @return
     */
    public String[] getAllDevicesPath() {
        Vector<String> devices = new Vector<>();
        // Parse each driver
        Iterator<Driver> itdriv = getDrivers().iterator();
        while (itdriv.hasNext()) {
            Driver driver = itdriv.next();
            Iterator<File> itdev = driver.getDevices().iterator();
            while (itdev.hasNext()) {
                String device = itdev.next().getAbsolutePath();
                devices.add(device);
            }
        }
        return devices.toArray(new String[devices.size()]);
    }
}
