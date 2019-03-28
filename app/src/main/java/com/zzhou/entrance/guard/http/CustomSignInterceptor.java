package com.zzhou.entrance.guard.http;

import com.zhouyou.http.interceptor.BaseDynamicInterceptor;
import com.zzhou.entrance.guard.MyApplication;

import java.util.TreeMap;

/**
 * <desc>
 * Created by The Moss on 2018/8/6.
 */

public class CustomSignInterceptor extends BaseDynamicInterceptor<CustomSignInterceptor> {

    public TreeMap<String, String> dynamic(TreeMap dynamicMap) {
//        LogUtils.d(">>>> mac = " + MacUtils.getMac());
        dynamicMap.put("deviceNo", MyApplication.getInstance().getDeviceNo());
        return dynamicMap;
    }
}
