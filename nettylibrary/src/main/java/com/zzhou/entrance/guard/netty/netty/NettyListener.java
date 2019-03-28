package com.zzhou.entrance.guard.netty.netty;


import com.zzhou.entrance.guard.netty.bean.MessageInfo;

/**
 * <desc>
 * Created by The Moss on 2018/8/30.
 */

public interface NettyListener {

    byte STATUS_CONNECT_SUCCESS = 1;

    byte STATUS_CONNECT_CLOSED = 2;

    byte STATUS_CONNECT_ERROR = 0;


    /**
     * 对消息的处理
     */
    void onMessageResponse(MessageInfo info);

    /**
     * 当服务状态发生变化时触发
     */
    void onServiceStatusConnectChanged(int statusCode);
    /*
    * 当状态异常触发
    */
    void onStateChanged(int erroCode);
}
