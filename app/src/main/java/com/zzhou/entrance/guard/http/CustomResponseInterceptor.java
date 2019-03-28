package com.zzhou.entrance.guard.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zhouyou.http.interceptor.BaseExpiredInterceptor;
import com.zzhou.entrance.guard.util.CipherUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * <desc>
 * Created by The Moss on 2018/8/13.
 */

public class CustomResponseInterceptor extends BaseExpiredInterceptor {
    Gson gson;

    public CustomResponseInterceptor() {
        gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .create();
    }

    @Override
    public boolean isResponseExpired(Response response, String bodyString) {
        return true;
    }

    @Override
    public Response responseExpired(Chain chain, String bodyString) {
        Request request = chain.request();
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String newBody = CipherUtils.decrypt(bodyString);
//        LogUtils.d("response = " + newBody);
        return response.newBuilder().body(ResponseBody.create(response.body().contentType(), newBody)).build();
    }
}
