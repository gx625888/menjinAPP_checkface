package com.zzhou.entrance.guard;

import android.content.Context;
import android.content.SharedPreferences;

import com.zzhou.entrance.guard.util.FileUtils;
import com.zzhou.entrance.guard.util.IOUtils;

import java.io.File;

/**
 * Created by 周振 on 2017/4/13.
 */

public class AppConfig {

    private static AppConfig appConfig;

    private SharedPreferences preferences;


    /**
     * App根目录.
     */
    public String APP_PATH_ROOT;

    private AppConfig() {
        preferences = App.INSTANCE.getSharedPreferences("Entrance_Guard", Context.MODE_PRIVATE);

        APP_PATH_ROOT = FileUtils.getRootPath().getAbsolutePath() + File.separator +
                "Entrance_Guard/";
    }

    public static AppConfig getInstance() {
        if (appConfig == null)
            synchronized (AppConfig.class) {
                if (appConfig == null)
                    appConfig = new AppConfig();
            }
        return appConfig;
    }

    public void initialize() {
        boolean iscreate = IOUtils.createFolder(APP_PATH_ROOT);
    }

    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).commit();
    }

    public int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).commit();
    }

    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).commit();
    }

    public long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public void putFloat(String key, float value) {
        preferences.edit().putFloat(key, value).commit();
    }

    public float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }
    public void remove(String key){
        preferences.edit().remove(key).commit();
    }
    public interface ShareKey{
    }
}

