package com.zzhou.entrance.guard.netty;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.zzhou.entrance.guard.MyApplication;
import com.zzhou.entrance.guard.netty.activity.ActivityManager;
import com.zzhou.entrance.guard.netty.activity.NettyActivity;
import com.zzhou.entrance.guard.netty.bean.MessageInfo;
import com.zzhou.entrance.guard.netty.bean.NettyBaseInfo;
import com.zzhou.entrance.guard.netty.bean.Register;
import com.zzhou.entrance.guard.netty.bean.RequestCmd;
import com.zzhou.entrance.guard.netty.netty.NettyClient;
import com.zzhou.entrance.guard.netty.netty.NettyListener;
import com.zzhou.entrance.guard.receive.MyAlarmReceiver;
import com.zzhou.entrance.guard.util.LogUtils;
import com.zzhou.entrance.guard.util.ToastShow;

import java.util.Stack;

/**
 * <desc>
 * Created by The Moss on 2018/8/30.
 */

public class NettyService extends Service implements NettyListener {

    private NetworkReceiver receiver;
    public static final String TAG = NettyService.class.getName();
    long KEEP_ALIVE_INTERVAL = 60 * 1000;

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
//        NotificationChannel channel = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            channel = new NotificationChannel("10000", "11000",
//                    NotificationManager.IMPORTANCE_HIGH);
//            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            manager.createNotificationChannel(channel);
//
//            Notification notification = new Notification.Builder(getApplicationContext(), "10000").build();
//            startForeground(1, notification);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, MyJobService.class));
//            builder.setMinimumLatency(TimeUnit.MILLISECONDS.toMillis(10)); //执行的最小延迟时间
//            builder.setOverrideDeadline(TimeUnit.MILLISECONDS.toMillis(60));  //执行的最长延时时间
//            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);  //任何网络状态
//            builder.setBackoffCriteria(TimeUnit.MINUTES.toMillis(10), JobInfo.BACKOFF_POLICY_LINEAR);  //线性重试方案
//            builder.setRequiresCharging(true); // 充电状态
//            jobScheduler.schedule(builder.build());
//        } else {
        LogUtils.d("NettyService  onCreare");
        Intent intent = new Intent(this, MyAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), KEEP_ALIVE_INTERVAL, pendingIntent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setWindow(AlarmManager.RTC, System.currentTimeMillis() + KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, pendingIntent);
//        } else {
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, pendingIntent);
//        }
//        }
        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NettyClient.getInstance().setListener(this);
        connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "NettyService onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
//        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().setConnectStatus(false);
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
        if (info.getCmd() == RequestCmd.RESET_APP) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ToastShow.show(getBaseContext(), "正在重启....");
                }
            });
            Intent intent = new Intent("com.zzhou.entrance.guard.RESET_MAIN");
            sendBroadcast(intent);
            return;
        }
        notifyData(NettyActivity.MSG_FROM_SERVER, info);
    }

    private void notifyData(int type, MessageInfo info) {
        final Stack<NettyActivity> activities = ActivityManager.getInstance().getActivities();
        if (activities != null && activities.size() < 1) {
            Intent intent = new Intent("com.zzhou.entrance.guard.RESET_MAIN");
            sendBroadcast(intent);
        }
        if (type == -1) {
            return;
        }
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
            LogUtils.d(TAG, "connect sucessful");
//            new Handler(getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getBaseContext(),"connect sucessful",Toast.LENGTH_SHORT).show();
//                }
//            });
            //远程重启后 页面设置为单任务时。服务重启，页面无法重启，没想到好的方法，暂时调用这个方法，当服务器重启成功后在重启一次，这是就可以拉起页面
            notifyData(-1, new MessageInfo());
            sendAuthor();
        } else {
            NettyClient.getInstance().setConnectStatus(false);
            LogUtils.d(TAG, "connect fail statusCode = " + statusCode);
//            new Handler(getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getBaseContext(),"connect fail statusCode = ",Toast.LENGTH_SHORT).show();
//                }
//            });
            notifyData(NettyActivity.MSG_NET_WORK_ERROR, null);
//            try {
//                Thread.sleep(1000);
//                connect();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }

    }

    @Override
    public void onStateChanged(int erroCode) {
        NettyClient.getInstance().setConnectStatus(false);
        LogUtils.d(TAG, "NettyService onStateChanged = " + erroCode);
    }

    /**
     * 发送认证信息
     */
    private void sendAuthor() {
        final Register registerInfo = new Register();
        LogUtils.d(TAG, ">>>> mac = " + MyApplication.getInstance().getDeviceNo());
//        LogUtils.d(TAG, ">>>> uuid = " + UUIDUtils.getShortUuid(getBaseContext()));
//        new Handler(getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getBaseContext(),">>>> mac = " + MacUtils.getMac(ConnectivityManager.TYPE_WIFI),Toast.LENGTH_SHORT).show();
//            }
//        });
        registerInfo.setDeviceNo(MyApplication.getInstance().getDeviceNo());
        final NettyBaseInfo<Register> baseInfo = new NettyBaseInfo();
        baseInfo.setCmd(NettyBaseInfo.CDM.register);
        baseInfo.setMsg("注册");
        baseInfo.setData(registerInfo);
        NettyClient.getInstance().sendMessage(baseInfo, null);
//        NettyClient.getInstance().sendMessage(baseInfo, new FutureListener() {
//            @Override
//            public void success() {
//
//            }
//
//            @Override
//            public void error() {
//                if (NettyClient.getInstance().getConnectStatus()) {
//                    try {
//                        Thread.sleep(6*1000);//6秒后重新注册
//                        Log.e(TAG,"regist fail once register");
//                        sendAuthor();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }else{
//                    Log.e(TAG,"server is dis connect reconnect");
//                    connect();
//                }
//            }
//        });
    }

    public void sendEMCIcast() {
        sendBroadcast(new Intent("Intent.ACTION_SCREEN_ON"));
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
                    LogUtils.e(TAG, "connecting ...");
                }
            }
        }
    }
}