package com.zzhou.entrance.guard;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.multidex.MultiDex;

import com.zhangke.zlog.ZLog;
import com.zzhou.entrance.guard.http.EasyUtils;
import com.zzhou.entrance.guard.netty.NettyService;
import com.zzhou.entrance.guard.util.MacUtils;

import cn.testin.analysis.data.TestinDataApi;
import cn.testin.analysis.data.TestinDataConfig;
import io.netty.util.internal.StringUtil;

/**
 * <desc>
 * Created by The Moss on 2018/8/29.
 */

public class MyApplication extends Application {
    static MyApplication instance;
    ContentResolver mResolver;
    private Process mLogProcess;
    private String deviceNo;

    @Override
    public void onCreate() {
        super.onCreate();
//        CrashHandler.getInstance().init(this);
//        LogUtils.initialize(this, true, Level.ALL);
        ZLog.Init(AppConfig.getInstance().APP_PATH_ROOT + "/log");
        ZLog.openSaveToFile();

//        try {
//            Calendar calendar = Calendar.getInstance();
//            String filename = String.format(AppConfig.getInstance().APP_PATH_ROOT + "/log_%04d%02d%02d_%02d%02d%02d.txt",
//                    calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH),
//                    calendar.get(Calendar.DAY_OF_MONTH),
//                    calendar.get(Calendar.HOUR_OF_DAY),
//                    calendar.get(Calendar.MINUTE),
//                    calendar.get(Calendar.SECOND));
//            mLogProcess = Runtime.getRuntime().exec(String.format("logcat -d time -f %s", filename));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        instance = this;
        mResolver = getContentResolver();
        AppConfig.getInstance().initialize();
        EasyUtils.initEasyHttp(this, Constants.SERVER_URL);
        initNetty();
        //设置启动参数
        TestinDataConfig testinDataConfig = new TestinDataConfig()
                .openShake(false)//设置是否打开摇一摇反馈bug功能
                .collectCrash(true)//设置是否收集app崩溃信息
                .collectANR(true)//设置是否收集ANR异常信息
                .collectLogCat(true)//设置是否收集logcat系统日志
                .collectUserSteps(true);//设置是否收集用户操作步骤
        //SDK初始化
        TestinDataApi.init(this, "2043ac7bc1384026d1c5e371c4abbf7d", testinDataConfig);
    }

    @Override
    public void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mLogProcess != null) {
            mLogProcess.destroy();
        }
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public ContentResolver getResovler() {
        return mResolver;
    }

    private void initNetty() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent inten = new Intent(getApplicationContext(), NettyService.class);
                startService(inten);
            }
        }).start();
    }

    public String getDeviceNo() {
        if (StringUtil.isNullOrEmpty(deviceNo)) {
            deviceNo = MacUtils.getMac(ConnectivityManager.TYPE_WIFI);
//            deviceNo = UUIDUtils.getShortUuid(getBaseContext());
        }
        return deviceNo;
    }
}
