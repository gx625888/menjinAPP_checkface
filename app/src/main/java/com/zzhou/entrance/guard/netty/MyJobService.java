//package com.zzhou.entrance.guard.netty;
//
//import android.annotation.TargetApi;
//import android.app.ActivityManager;
//import android.app.job.JobInfo;
//import android.app.job.JobParameters;
//import android.app.job.JobScheduler;
//import android.app.job.JobService;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.util.Log;
//
//import java.util.List;
//
///**
// * <desc>
// * Created by The Moss on 2018/9/10.
// */
//
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//public class MyJobService extends JobService {
//
//    private int JobId = 0;
//
//    @Override
//    public boolean onStartJob(JobParameters params) {
//        Log.d("MyJobService","onStartJob");
//        boolean isLocalServiceWork = isServiceWork(this, NettyService.class.getName());
//        if (!isLocalServiceWork) {
//            startService(new Intent(this, NettyService.class));
//        }
//        return false;
//    }
//
//    // 如果确定停止系统调度作业，即使调度作业可能被完成，将调用此方法
//    @Override
//    public boolean onStopJob(JobParameters params) {
//        Log.d("MyJobService","onStopJob");
//        boolean isLocalServiceWork = isServiceWork(this, NettyService.class.getName());
//        if (!isLocalServiceWork) {
//            startService(new Intent(this, NettyService.class));
//        }
//        return false;
//    }
//
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Log.d("MyJobService","onTaskRemoved");
//        boolean isLocalServiceWork = isServiceWork(this, NettyService.class.getName());
//        if (!isLocalServiceWork) {
//            startService(new Intent(this, NettyService.class));
//        }
//    }
//
//    //将任务作业发送到作业调度中去
//    public void scheduleJob(JobInfo t) {
//        Log.i("MyJobService", "调度job");
//        JobScheduler tm =
//                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.schedule(t);
//    }
//    public JobInfo getJobInfo(){
//        JobInfo.Builder builder = new JobInfo.Builder(JobId++, new ComponentName(this, MyJobService.class));
//        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
//        builder.setPersisted(true);
//        builder.setRequiresCharging(false);
//        builder.setRequiresDeviceIdle(false);
//        //间隔100毫秒
//        builder.setPeriodic(100);
//        return builder.build();
//    }
//
//    // 判断服务是否正在运行
//    public boolean isServiceWork(Context mContext, String serviceName) {
//        boolean isWork = false;
//        ActivityManager myAM = (ActivityManager) mContext
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
//        if (myList.size() <= 0) {
//            return false;
//        }
//        for (int i = 0; i < myList.size(); i++) {
//            String mName = myList.get(i).service.getClassName().toString();
//            if (mName.equals(serviceName)) {
//                isWork = true;
//                break;
//            }
//        }
//        return isWork;
//    }
//}