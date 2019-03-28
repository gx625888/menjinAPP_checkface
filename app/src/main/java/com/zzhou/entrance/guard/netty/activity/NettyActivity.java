package com.zzhou.entrance.guard.netty.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;

import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;
import com.zzhou.entrance.guard.Constants;
import com.zzhou.entrance.guard.MyApplication;
import com.zzhou.entrance.guard.netty.HostUtils;
import com.zzhou.entrance.guard.netty.bean.MessageInfo;
import com.zzhou.entrance.guard.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import cn.testin.analysis.bug.BugOutApi;

/**
 * <desc>
 * Created by The Moss on 2018/8/29.
 */

public abstract class NettyActivity extends FragmentActivity {
    public final static int MSG_FROM_SERVER = 0x1;
    public final static int MSG_NET_WORK_ERROR = 0x2;
    protected String TAG;
    protected MHandler handler;

    private static boolean isSync = false;

    /**
     * 暴露handler给Service
     *
     * @return
     */
    public MHandler getHandler() {
        return handler;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "***********onCreate*************");
        ActivityManager.getInstance().addActivity(this);
        handler = new MHandler(this);
        TAG = this.getClass().getName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "***********onResume*************");
        BugOutApi.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d(TAG, "***********onPause*************");
        //注：回调 2
        BugOutApi.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "***********onDestroy*************");
        ActivityManager.getInstance().removeActivity(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //注：回调 3
        BugOutApi.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    public static class MHandler extends Handler {
        private WeakReference<NettyActivity> activity;

        public MHandler(NettyActivity activity) {
            this.activity = new WeakReference<NettyActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtils.d("获取IM Message，" + msg.toString());
            if (activity == null || activity.get() == null) return;
            final NettyActivity nettyActivity = activity.get();
            switch (msg.what) {
                case NettyActivity.MSG_FROM_SERVER:
                    nettyActivity.notifyData((MessageInfo) msg.obj);
                    break;
                case NettyActivity.MSG_NET_WORK_ERROR:
                    if (isSync) {
                        return;
                    }
                    isSync = true;
                    EasyHttp.post(Constants.Api.GET_SERVIER)
                            .execute(new SimpleCallBack<String>() {
                                @Override
                                public void onError(ApiException e) {
                                    LogUtils.e("获取IM端口异常，" + e.getMessage());
                                    isSync = false;
                                }

                                @Override
                                public void onSuccess(String result) {
                                    try {
                                        JSONObject json = new JSONObject(result);
                                        if (json.getInt("code") == 0) {
//                                                FileUtils.deleteFile(AppConfig.getInstance().APP_PATH_ROOT + "/url");
                                                JSONObject obj = json.getJSONObject("data");
                                                int port = obj.getInt("port");
                                                String ip = obj.getString("ip");
//                                                if (port > 0 && !StringUtil.isNullOrEmpty(ip)) {
//                                                    UrlConstant.SOCKET_HOST = ip;
//                                                    UrlConstant.SOCKET_PORT = port;
//                                                }
                                                try {
                                                    HostUtils.saveHost(MyApplication.getInstance(),result,ip,port);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            LogUtils.e("获取IM端口异常，" + json.getString("msg"));
                                        }
                                        isSync = false;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    break;
            }
        }
    }

    /**
     * 在主线程通知子类 刷新UI
     *
     * @param message
     */
    protected abstract void notifyData(MessageInfo message);
}
