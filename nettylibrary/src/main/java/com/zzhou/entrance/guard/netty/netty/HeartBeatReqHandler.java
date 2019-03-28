package com.zzhou.entrance.guard.netty.netty;

import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * <desc>
 * Created by The Moss on 2018/8/29.
 */

public class HeartBeatReqHandler extends ChannelHandlerAdapter {
    private static final String TAG = HeartBeatReqHandler.class.getName();

    //线程安全心跳失败计数器
    private AtomicInteger unRecPongTimes = new AtomicInteger(1);

    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
//        NettyMessageProto message = (NettyMessageProto)msg;
//        //服务器端心跳回复
//        if(message.getHeader() != null  && message.getHeader().getType() == Constants.MSGTYPE_HEARTBEAT_RESPONSE){
//            //如果服务器进行pong心跳回复，则清零失败心跳计数器
//            unRecPongTimes = new AtomicInteger(1);
//            Log.d(TAG,"client receive server pong msg :---->"+message);
//        }else{
//            ctx.fireChannelRead(msg);
//        }
    }

    /**
     * 事件触发器，该处用来处理客户端空闲超时,发送心跳维持连接。
     */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                /*读超时*/
                Log.i(TAG,"===客户端===(READER_IDLE 读超时)");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                /*客户端写超时*/
                Log.i(TAG,"===客户端===(WRITER_IDLE 写超时)");
                unRecPongTimes.getAndIncrement();
                //服务端未进行pong心跳响应的次数小于3,则进行发送心跳，否则则断开连接。
                if(unRecPongTimes.intValue() < 3){
                    //发送心跳，维持连接
//                    ctx.channel().writeAndFlush(buildHeartBeat()) ;
                    Log.i(TAG,"客户端：发送心跳");
                }else{
                    ctx.channel().close();
                }
            } else if (event.state() == IdleState.ALL_IDLE) {
                /*总超时*/
                Log.i(TAG,"===客户端===(ALL_IDLE 总超时)");
            }
        }
    }

//    private NettyMessageProto buildHeartBeat(){
//        HeaderProto header = HeaderProto.newBuilder().setType(Constants.MSGTYPE_HEARTBEAT_REQUEST).build();
//        NettyMessageProto  message = NettyMessageProto.newBuilder().setHeader(header).build();
//        return message;
//    }

    /**
     * 异常处理
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception{
        ctx.fireExceptionCaught(cause);
    }

}

