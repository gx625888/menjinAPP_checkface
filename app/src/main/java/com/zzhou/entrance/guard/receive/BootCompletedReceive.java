package com.zzhou.entrance.guard.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zzhou.entrance.guard.module.MainActivity;
import com.zzhou.entrance.guard.util.LogUtils;

/**
 * <desc>
 * Created by The Moss on 2018/9/10.
 */

public class BootCompletedReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent newIntent = new Intent(context, MainActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
            context.startActivity(newIntent);

            //NettyService.class就是要启动的服务
//            Intent service = new Intent(context, NettyService.class);
//            context.startService(service);
//            Log.v("TAG", "开机自动服务自动启动.....");
//            //启动应用，参数为需要自动启动的应用的包名
//            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.zzhou.entrance.guard");
//            context.startActivity(intent );
        } else if (intent.getAction().equals("com.zzhou.entrance.guard.RESET_MAIN")) {
            LogUtils.d("设备重启");
            Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
            // 重新启动APP
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }
}
