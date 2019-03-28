package com.zzhou.entrance.guard.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zzhou.entrance.guard.netty.NettyService;
import com.zzhou.entrance.guard.netty.netty.NettyClient;
import com.zzhou.entrance.guard.util.LogUtils;

/**
 * <desc>
 * Created by The Moss on 2018/9/10.
 */

public class MyAlarmReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        LogUtils.d("MyAlarmReceiver once time");
        if (!NettyClient.getInstance().getConnectStatus()) {
            Intent startIntent = new Intent(context, NettyService.class);
            context.startService(startIntent);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//重新设置广播 实现无限循环 19以上
//            setAlarmTime(context, System.currentTimeMillis() + 15, "自定义action", 15);
//        }
    }

}
