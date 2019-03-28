package com.zzhou.entrance.guard.http;

import android.app.Application;

import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.cache.converter.GsonDiskConverter;
import com.zhouyou.http.cache.model.CacheMode;
import com.zhouyou.http.model.HttpHeaders;
import com.zhouyou.http.model.HttpParams;
import com.zzhou.entrance.guard.BuildConfig;
import com.zzhou.entrance.guard.MyApplication;

import retrofit2.converter.gson.GsonConverterFactory;

/**
 * <desc>
 * Created by The Moss on 2018/7/23.
 */

public class EasyUtils {
    public static void initEasyHttp(Application application, String host) {
        EasyHttp.init(application);//默认初始化,必须调用

        //全局设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json");
        headers.put("Accept-Charset", "utf-8");
//        //全局设置请求参数
        HttpParams params = new HttpParams();
        params.put("deviceNo", MyApplication.getInstance().getDeviceNo());
//        params.put("origin", Constants.DEVICE + ",4.8.7");

        //以下设置的所有参数是全局参数,同样的参数可以在请求的时候再设置一遍,那么对于该请求来讲,请求中的参数会覆盖全局参数
        EasyHttp.getInstance()

                //可以全局统一设置全局URL
                .setBaseUrl(host)//设置全局URL

                // 打开该调试开关并设置TAG,不需要就不要加入该行
                // 最后的true表示是否打印okgo的内部异常，一般打开方便调试错误
                .debug("EasyHttp", BuildConfig.DEBUG)

                //如果使用默认的6秒,以下三行也不需要设置
                .setReadTimeOut(4 * 1000)
                .setWriteTimeOut(4 * 1000)
                .setConnectTimeout(4 * 1000)

                //可以全局统一设置超时重连次数,默认为3次,那么最差的情况会请求4次(一次原始请求,三次重连请求),
                //不需要可以设置为0
                .setRetryCount(1)//网络不好自动重试2次
                //可以全局统一设置超时重试间隔时间,默认为500ms,不需要可以设置为0
                .setRetryDelay(500)//每次延时500ms重试
                //可以全局统一设置超时重试间隔叠加时间,默认为0ms不叠加
                .setRetryIncreaseDelay(500)//每次延时叠加500ms

                //可以全局统一设置缓存模式,默认是不使用缓存,可以不传,具体请看CacheMode
                .setCacheMode(CacheMode.NO_CACHE)
                //可以全局统一设置缓存时间,默认永不过期
                .setCacheTime(-1)//-1表示永久缓存,单位:秒 ，Okhttp和自定义RxCache缓存都起作用
                //全局设置自定义缓存保存转换器，主要针对自定义RxCache缓存
//                .setCacheDiskConverter(new SerializableDiskConverter())//默认缓存使用序列化转化
                .setCacheDiskConverter(new GsonDiskConverter())
                //全局设置自定义缓存大小，默认50M
                .setCacheMaxSize(100 * 1024 * 1024)//设置缓存大小为100M
                //设置缓存版本，如果缓存有变化，修改版本后，缓存就不会被加载。特别是用于版本重大升级时缓存不能使用的情况
                .setCacheVersion(1)//缓存版本为1
                .addCommonHeaders(headers)//设置全局公共头
                .addCommonParams(params)//设置全局公共参数
                .addConverterFactory(GsonConverterFactory.create())
                //.setHttpCache(new Cache())//设置Okhttp缓存，在缓存模式为DEFAULT才起作用
                //可以设置https的证书,以下几种方案根据需要自己设置
                .setCertificates();                 //方法一：信任所有证书,不安全有风险
//                .addInterceptor(new CustomSignInterceptor());//添加参数签名拦截器
//                .addInterceptor(new CustomResponseInterceptor());//添加返回拦截器
    }
}
