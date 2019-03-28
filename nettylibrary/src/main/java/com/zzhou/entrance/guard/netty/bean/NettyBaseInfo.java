package com.zzhou.entrance.guard.netty.bean;

public class NettyBaseInfo<T> {
    /**
     * 命令号
     * 1-0.心跳
     * 1-1.注册
     * 1-2.消息
     */
    private int ret;

    /*
        * 消息说明
        */
    private String msg;

    /**
     * 命令对象
     */
    private T data;

    public NettyBaseInfo(int ret, T data) {
        this.ret = ret;
        this.data = data;
    }

    public NettyBaseInfo() {
    }

    public int getRet() {
        return ret;
    }

    public void setCmd(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public interface CDM {
        int heat = 0;
        int register = 1;
        int message = 2;
    }
}
