package com.zzhou.entrance.guard.util;

import android.net.ConnectivityManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;

import io.netty.util.internal.StringUtil;

/**
 * <desc>
 * Created by The Moss on 2018/9/21.
 */
public class MacUtils {
    /**
     * 获取手机的MAC地址
     *
     * @return
     */
    public static String getMac() {
        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat/sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.replaceAll(" ", "");// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        if (macSerial == null || "".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
//                e.printStackTrace();
            }

        }
        if (macSerial == null || "".equals(macSerial)) {
            return "xx-001";
        }
        return macSerial;
    }

    public static String getMac(int type) {
        String mac = null;
        switch (type) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_ETHERNET:
                StringBuffer fileData = new StringBuffer(16);
                try {
                    BufferedReader reader;
                    if (type == ConnectivityManager.TYPE_WIFI)
                        reader = new BufferedReader(new FileReader("/sys/class/net/wlan0/address"));
                    else
                        reader = new BufferedReader(new FileReader("/sys/class/net/eth0/address"));

                    char[] buf = new char[16];
                    int numRead = 0;
                    while ((numRead = reader.read(buf)) != -1) {
                        String readData = String.valueOf(buf, 0, numRead);
                        fileData.append(readData);
                    }
                    reader.close();
                    mac = fileData.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

//            case ConnectivityManager.TYPE_BLUETOOTH:
//                mac = Settings.Secure.getString(getContentResolver(), "bluetooth_address");
//                break;

            default:
                break;
        }
        if (StringUtil.isNullOrEmpty(mac)) {
            return "uy0iTYfVGkhpC3yi";
//            return UUIDUtils.getShortUuid(MyApplication.getInstance());
        }
        mac = mac.replaceAll("\\\\n", "")
                .replaceAll("\\n", "")
                .trim();
        return mac;
    }

    public static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    public static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }
}