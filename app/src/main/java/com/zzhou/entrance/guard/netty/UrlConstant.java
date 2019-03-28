package com.zzhou.entrance.guard.netty;

/**
 * <desc>
 * Created by The Moss on 2018/8/30.
 */

public class UrlConstant {
    /**
     * socket 服务器IP地址
     */
//    public static String SOCKET_HOST = "118.31.43.189";
    public static String SOCKET_HOST = "";
    /**
     *  socket 服务器端口号
     */
//    public static int SOCKET_PORT = 8081;
    public static int SOCKET_PORT = 0;

    public String getSOCKET_HOST() {
        return SOCKET_HOST;
    }

    public void setSOCKET_HOST(String SOCKET_HOST) {
        this.SOCKET_HOST = SOCKET_HOST;
    }

    public int getSOCKET_PORT() {
        return SOCKET_PORT;
    }

    public void setSOCKET_PORT(int SOCKET_PORT) {
        this.SOCKET_PORT = SOCKET_PORT;
    }
}
