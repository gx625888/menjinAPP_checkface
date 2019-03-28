package com.zzhou.entrance.guard;

/**
 * <desc>
 * Created by The Moss on 2018/9/7.
 */

public interface Constants {

//    String SERVER_URL = "http://221.130.6.212:4888/access/api/";//测试
    String SERVER_URL = "http://47.101.175.155:8080/access/api/";

    static final int MODE_CLOSE = -1;//开门结束
    static final int MODE_CARD = 0;//刷卡
    static final int MODE_WXPASS = 1;//微信
    static final int MODE_REMOTE = 2;//远程
    static final int MODE_CALL = 3;//呼叫
    static final int MODE_SENDWX = 4;//呼叫时抓拍照片，推送到微信公众号

    interface Api{
        /*屏保广告 图片\视频*/
        String SCREEN_ADS = "screen/ads.do";
        /*上传开门信息*/
        String UPLOAD_INFOS = "upload/infos.do";
        /*版本更新*/
        String UPDATE = "update.do";
        /*上传告警信息*/
        String UPDATE_WARN = "upload/warn.do";
        /*同步用户数据*/
        String UPDATE_ACCOUNTS = "update/accounts.do";
        /*获取IM服务器端口号*/
        String GET_SERVIER = "socket/getserver.do";
        /*验证开门密码*/
        String VALIDATE_PASS = "validate/pass.do";
        /*获取呼叫中心主机分机账号密码*/
        String TEL = "tel.do";
        /*获取户主电话信息*/
        String UPDATAE_HOUSER = "update/houses.do";
    }
    interface CallState{
        //incoming,calling,connecting,comfirmed,invalid,disconnected.
        String INCOMING = "incoming...";//来电
        String CALLING = "calling...";//呼叫中
        String COMFIRMED = "confirmed...";//接听中
        String CONNECTING = "connecting...";//通话中
        String INVALID = "invalid...";//无法接通
        String DISCONNECTED = "disconnected...";//挂断
    }
}
