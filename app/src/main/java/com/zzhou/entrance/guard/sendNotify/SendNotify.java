package com.zzhou.entrance.guard.sendNotify;


import android.content.Context;
import android.content.SharedPreferences;

import com.zzhou.entrance.guard.Constants;
import com.zzhou.entrance.guard.module.MainActivity;
import com.zzhou.entrance.guard.util.LogUtils;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 发送通知
 */
public class SendNotify {
    private static final String TAG =SendNotify.class.getName();

    /**
     * 请求SendNotify
     * @param flag
     * @param houseNo
     * @param mess
     */
    public static void doorsendNotify(final int flag,final String houseNo, final String mess){
        final String open_type = String.valueOf(flag);
        LogUtils.d(TAG,"------开始请求SendNotify------:"+flag+"-"+houseNo);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //生成参数,创建一个请求实体对象 RequestBody
                    RequestBody params = new FormBody.Builder()
                            .add("flag", "1")
                            .add("opentype", open_type)
                            .add("houseNo", houseNo)
                            .build();
                    //创建OkHttpClient对象
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://47.101.175.155:2280/door/sendNotify.itf")
                            .post(params)
                            .build();//创建Request 对象
                    Response response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()){
                        LogUtils.d(TAG,"----------请求SendNotify: 成功-----------"+mess+"------------");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
