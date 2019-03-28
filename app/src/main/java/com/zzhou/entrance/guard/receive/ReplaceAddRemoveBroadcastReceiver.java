package com.zzhou.entrance.guard.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.zzhou.entrance.guard.util.LogUtils;

/**
 * <desc>
 * Created by The Moss on 2018/10/16.
 */

public class ReplaceAddRemoveBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null && context.getPackageName().equals(data.getEncodedSchemeSpecificPart())) {

                LogUtils.d("更新安装成功.....");
                Toast.makeText(context, "更新安装成功", Toast.LENGTH_LONG).show();
                // 重新启动APP
                Intent intentToStart = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                context.startActivity(intentToStart);
            }
        }
    }
}