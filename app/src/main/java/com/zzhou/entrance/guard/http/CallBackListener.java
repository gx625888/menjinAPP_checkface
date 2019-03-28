package com.zzhou.entrance.guard.http;

/**
 * <desc>
 * Created by The Moss on 2018/9/14.
 */

public interface CallBackListener {
    void onResult(boolean isSuccess, Object result);
}
