package com.zzhou.entrance.guard.netty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * <desc>
 * Created by The Moss on 2018/9/10.
 */

public class MyAlarmReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        Intent startIntent = new Intent(context, NettyService.class);
        context.startService(startIntent);
    }

}
