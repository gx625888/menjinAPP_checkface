package com.zzhou.entrance.guard.netty.netty;

import android.util.Log;

import com.google.gson.Gson;
import com.zzhou.entrance.guard.netty.bean.MessageInfo;
import com.zzhou.entrance.guard.netty.bean.NettyBaseInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * <desc>
 * Created by The Moss on 2018/8/30.
 */

public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
    private static final String TAG = NettyClientHandler.class.getName();
    private NettyListener listener;
    //线程安全心跳失败计数器
    private AtomicInteger unRecPongTimes = new AtomicInteger(1);

    public NettyClientHandler(NettyListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyClient.getInstance().setConnectStatus(true);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyClient.getInstance().setConnectStatus(false);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
        NettyClient.getInstance().reconnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String byteBuf) {
        Log.e(TAG, "来自服务器的消息 ====》" + byteBuf);
        try {
            JSONObject json = new JSONObject(byteBuf);
            if (json.getInt("ret") == NettyBaseInfo.CDM.heat) {
                unRecPongTimes.set(1);
                return;
            }
            if (json.getInt("ret") == NettyBaseInfo.CDM.register) {
                if ("success".equals(json.getString("msg"))) {
                    NettyClient.getInstance().setAuthor(true);
                    Log.d(TAG,"register success");
                }else{
                    Log.d(TAG,"register fail");
                    NettyClient.getInstance().reconnect();
                }
                return;
            }
            if (json.getInt("ret") == NettyBaseInfo.CDM.message) {
                String messg = json.getJSONObject("data").toString();
                MessageInfo info = new Gson().fromJson(messg,MessageInfo.class);
                if (info != null) {
                    listener.onMessageResponse(info);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //利用写空闲发送心跳检测消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state() == IdleState.READER_IDLE) {
//                /*读超时*/
//                Log.i(TAG,"===客户端===(READER_IDLE 读超时)");
//            }else
            if (event.state() == IdleState.WRITER_IDLE || event.state() == IdleState.READER_IDLE) {
                NettyBaseInfo info = new NettyBaseInfo();
                info.setCmd(0);
                info.setMsg("心跳");
                String msg = new Gson().toJson(info);
//                ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                unRecPongTimes.getAndIncrement();
                //服务端未进行pong心跳响应的次数小于4,则进行发送心跳，否则则断开连接,断开后会自动重连。
                if (unRecPongTimes.intValue() < 4) {
                    //发送心跳，维持连接
                    ctx.channel().writeAndFlush(msg);
                    Log.i(TAG, "客户端：发送心跳");
                }else {
                    ctx.channel().close();
                }
            } else if (event.state() == IdleState.ALL_IDLE) {
                /*总超时*/
                Log.i(TAG, "===客户端===(ALL_IDLE 总超时)");
                ctx.channel().close();
            }
        }
    }

    //异常回调,默认的exceptionCaught只会打出日志，不会关掉channel
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        listener.onStateChanged(NettyListener.STATUS_CONNECT_ERROR);
        cause.printStackTrace();
        ctx.close();
    }
}
