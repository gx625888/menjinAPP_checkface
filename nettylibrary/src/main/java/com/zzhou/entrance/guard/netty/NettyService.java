package com.zzhou.entrance.guard.netty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.zzhou.entrance.guard.netty.activity.ActivityManager;
import com.zzhou.entrance.guard.netty.activity.NettyActivity;
import com.zzhou.entrance.guard.netty.bean.MessageInfo;
import com.zzhou.entrance.guard.netty.bean.NettyBaseInfo;
import com.zzhou.entrance.guard.netty.bean.Register;
import com.zzhou.entrance.guard.netty.netty.FutureListener;
import com.zzhou.entrance.guard.netty.netty.NettyClient;
import com.zzhou.entrance.guard.netty.netty.NettyListener;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

/**
 * <desc>
 * Created by The Moss on 2018/8/30.
 */

public class NettyService extends Service implements NettyListener {

    private NetworkReceiver receiver;
    public static final String TAG = NettyService.class.getName();
    long KEEP_ALIVE_INTERVAL = 60*1000;

    public NettyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, NettyService.class));
            builder.setMinimumLatency(TimeUnit.MILLISECONDS.toMillis(10)); //执行的最小延迟时间
            builder.setOverrideDeadline(TimeUnit.MILLISECONDS.toMillis(15));  //执行的最长延时时间
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);  //非漫游网络状态
            builder.setBackoffCriteria(TimeUnit.MINUTES.toMillis(10),  JobInfo.BACKOFF_POLICY_LINEAR);  //线性重试方案
            builder.setRequiresCharging(false); // 未充电状态
            jobScheduler.schedule(builder.build());
        } else {
            Intent intent = new Intent(this,MyAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, pendingIntent);
        }

        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NettyClient.getInstance().setListener(this);
        connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"NettyService onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();
    }

    private void connect() {
        if (!NettyClient.getInstance().getConnectStatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();//连接服务器
                }
            }).start();
        }
    }

    @Override
    public void onMessageResponse(MessageInfo info) {
        notifyData(NettyActivity.MSG_FROM_SERVER, info);
    }

    private void notifyData(int type, MessageInfo info) {
        final Stack<NettyActivity> activities = ActivityManager.getInstance().getActivities();
        for (NettyActivity activity : activities) {
            if (activity == null || activity.isFinishing()) {
                continue;
            }
            Message message = Message.obtain();
            message.what = type;
            message.obj = info;
            activity.getHandler().sendMessage(message);
        }
    }

    @Override
    public void onServiceStatusConnectChanged(int statusCode) {
        if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
            Log.e(TAG, "connect sucessful");
            sendAuthor();
        } else {
            Log.e(TAG, "connect fail statusCode = " + statusCode);
//            notifyData(NettyActivity.MSG_NET_WORK_ERROR, String.valueOf("服务器连接失败"));
            connect();
        }

    }

    @Override
    public void onStateChanged(int erroCode) {
        Log.e(TAG, "NettyService onStateChanged = " + erroCode);
    }

    /**
     * 发送认证信息
     */
    private void sendAuthor() {
        final Register registerInfo = new Register();
        registerInfo.setDeviceNo("123445566");
        final NettyBaseInfo<Register> baseInfo = new NettyBaseInfo<>();
        baseInfo.setCmd(NettyBaseInfo.CDM.register);
        baseInfo.setMsg("注册");
        baseInfo.setData(registerInfo);
        NettyClient.getInstance().sendMessage(baseInfo, new FutureListener() {
            @Override
            public void success() {

            }

            @Override
            public void error() {
                if (NettyClient.getInstance().getConnectStatus()) {
                    try {
                        Thread.sleep(6*1000);//6秒后重新注册
                        Log.e(TAG,"regist fail once register");
                        sendAuthor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.e(TAG,"server is dis connect reconnect");
                    connect();
                }
            }
        });
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    connect();
                    Log.e(TAG, "connecting ...");
                }
            }
        }
    }
}