package com.zzhou.entrance.guard.netty;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

/**
 * <desc>
 * Created by The Moss on 2018/9/10.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        startService(new Intent(this,NettyService.class));
        return false;
    }

    // 如果确定停止系统调度作业，即使调度作业可能被完成，将调用此方法
    @Override
    public boolean onStopJob(JobParameters params) {
        startService(new Intent(this,NettyService.class));
        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        startService(new Intent(this,NettyService.class));
    }
}