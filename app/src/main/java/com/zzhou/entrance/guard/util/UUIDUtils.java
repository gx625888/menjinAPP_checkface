package com.zzhou.entrance.guard.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;

import io.netty.util.internal.StringUtil;

/**
 * <desc>生成16位的唯一编码，并且保存本地文件，SharedPreferences ,如果存在不重新生成
 * Created by The Moss on 2018/10/24.
 */

public class UUIDUtils {
    private static String uuidFile = FileUtils.getRootPath() + "/Android/.uuid";
    private static String uuidKey = "uuidkey";

    public static String[] chars = new String[]{"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    public static String getShortUuid(Context context) {
        String uuid = "";
        SharedPreferences preferences = context.getSharedPreferences("entrance_guard", context.MODE_PRIVATE);
        uuid = preferences.getString(uuidKey, "");
        if (!StringUtil.isNullOrEmpty(uuid)) {
            return uuid;
        }
        uuid = checkAndroidFile();
        if (!StringUtil.isNullOrEmpty(uuid)) {
            preferences.edit().putString(uuidKey, uuid).commit();
            return uuid;
        }
        uuid = reSetShortUuid(context);
        return uuid;
    }

    public static String reSetShortUuid(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("entrance_guard", context.MODE_PRIVATE);
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 16; i++) {
            String str = uuid.substring(i * 2, i * 2 + 2);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        preferences.edit().putString(uuidKey, shortBuffer.toString()).commit();
        saveAndroidFile(shortBuffer.toString());
        return shortBuffer.toString();
    }

    private static String checkAndroidFile() {
        BufferedReader reader = null;
        try {
            File file = new File(uuidFile);
            if (!file.exists()) {
                return "";
            }
            reader = new BufferedReader(new FileReader(file));
            return reader.readLine();
        } catch (Exception e) {
            return "";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveAndroidFile(String id) {
        try {
            File file = new File(uuidFile);
            if (FileUtils.fileExists(uuidFile)) {
                file.delete();
            }
            IOUtils.createFile(uuidFile);
            FileWriter writer = new FileWriter(file);
            writer.write(id);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
