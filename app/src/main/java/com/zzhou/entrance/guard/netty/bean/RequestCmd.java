package com.zzhou.entrance.guard.netty.bean;

/**
 * <desc>
 * Created by The Moss on 2018/9/13.
 */

public interface RequestCmd {
    //更新版本
    int UPDATE_VERSION = 0x1001;
    //更新广告
    int UPDATE_ADS = 0x1002;
    //获取用户信息
    int UPDATE_ACCOUNTS = 0x1003;
    //远程开门双开
    int OPEN_DOOR = 0x1004;
    //远程开门1
    int OPEN_DOOR1 = 0x1005;
    //远程开门2
    int OPEN_DOOR2 = 0x1006;
    //重启
    int RESET_APP = 0x1007;
}
