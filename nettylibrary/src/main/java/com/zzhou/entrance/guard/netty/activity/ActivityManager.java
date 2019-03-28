package com.zzhou.entrance.guard.netty.activity;

import java.util.Stack;

/**
 * <desc>
 * Created by The Moss on 2018/8/29.
 */

public class ActivityManager {

    private static ActivityManager acitivityManager = new ActivityManager();
    public Stack<NettyActivity> activities = new Stack<>();

    public static ActivityManager getInstance() {
        return acitivityManager;
    }

    public Stack<NettyActivity> getActivities() {
        return activities;
    }

    public void addActivity(NettyActivity activity) {
        if (activity == null) {
            return;
        }
        activities.add(activity);
    }

    public void removeActivity(NettyActivity activity) {
        if (activity == null) {
            return;
        }
        activities.remove(activity);
    }
}
