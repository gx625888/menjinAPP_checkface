package com.zzhou.entrance.guard.netty;

import android.content.Context;
import android.content.SharedPreferences;

import com.zzhou.entrance.guard.util.SystemTool;

import io.netty.util.internal.StringUtil;

/**
 * <desc>
 * Created by The Moss on 2018/11/12.
 */

public class HostUtils {

    public static String[] getHost(Context context) {
        try {
            SharedPreferences preferences = context.getSharedPreferences("entrance_guard", context.MODE_PRIVATE);
            int version = preferences.getInt("version",0);
            String ip = preferences.getString("ip", "");
            int port = preferences.getInt("port", 0);

            if (version != SystemTool.getAppVersionCode(context)) {
                return null;
            }
            if (port > 0 && !StringUtil.isNullOrEmpty(ip)) {
                return new String[]{ip, port + ""};
            }
//            else {
//                String result = (String) FileUtils.loadObject(AppConfig.getInstance().APP_PATH_ROOT + "/url");
//                JSONObject json = new JSONObject(result);
//                JSONObject obj = json.getJSONObject("data");
//                port = obj.getInt("port");
//                ip = obj.getString("ip");
//                if (port > 0 && !StringUtil.isNullOrEmpty(ip)) {
//                    return new String[]{ip, port + ""};
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveHost(Context context, String result, String ip, int port) {
//        try {
//            FileUtils.deleteFile(AppConfig.getInstance().APP_PATH_ROOT + "/url");
//            FileUtils.saveObject(result, AppConfig.getInstance().APP_PATH_ROOT + "/url");
        SharedPreferences preferences = context.getSharedPreferences("entrance_guard", context.MODE_PRIVATE);
        preferences.edit().putInt("version", SystemTool.getAppVersionCode(context)).commit();
        preferences.edit().putString("ip", ip).commit();
        preferences.edit().putInt("port", port).commit();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
