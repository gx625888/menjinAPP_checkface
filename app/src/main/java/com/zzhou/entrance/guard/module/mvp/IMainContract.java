package com.zzhou.entrance.guard.module.mvp;

import com.zzhou.entrance.guard.bean.AccountData;
import com.zzhou.entrance.guard.bean.Ads;
import com.zzhou.entrance.guard.bean.ImeiNo;
import com.zzhou.entrance.guard.http.CallBackListener;

import java.io.File;
import java.util.List;

/**
 * <desc>
 * Created by The Moss on 2018/9/14.
 */

public interface IMainContract {
    interface IView {
        /*广告更新*/
        void channelAdsResult(boolean isSuccess, List<Ads> ads);

        /*验证刷卡结果返回*/
        void validateAccountResult(boolean isSuccess, AccountData account);

        /*验证密码*/
        void validatePass(boolean isSuccess,String msg);

        /*获取呼叫中心账号*/
        void imeiAccountResult(boolean isSuccess, ImeiNo imei, String msg);

        /*发起呼叫请求*/
        void callAccountResult(boolean isSuccess, String msg, int which);

        /*发起手机呼叫请求*/
        void callMobileAccountResult(boolean isSuccess, String msg, int which);
    }

    interface IPresenter {
        void getLocalAdsInfo();

        void getAdsInfo();

        void validateCard(String cardid);

        void uploadOpenInfo(int mode, String cardNo, File file);

        void updateVersion();

        void uploadWarnInfo(int mode);

        void getAccounts();

        void getHouses();

        void validatePass(String pass);

        void imeiAccount();

        /*呼叫房间*/
        void callAccount(String houseNo);

        /*呼叫手机号*/
        void callMobileAccount(String mobileNo);

    }

    interface IModel {
        /*获取广告*/
        void getAdsInfo(CallBackListener callBack);

        /*mode 开门方式，file 抓拍照片*/
        void uploadOpenInfo(int mode, String cardNo, File file, CallBackListener callBack);

        /*版本更新*/
        void updateVersion();

        /*上传告警信息mode 告警类型火警，门未关*/
        void uploadWarnInfo(int mode, CallBackListener callBack);

        /*获取用户信息*/
        void getAccounts();

        /*获取户信息*/
        void getHouses();

        /*验证卡号*/
        void validateCard(String cardid, CallBackListener callBack);

        /*验证开门密码*/
        void validatePass(String pass, CallBackListener callBack);

        void imeiAccount(CallBackListener callBack);

        /*呼叫房间*/
        void callAccount(String houseNo, CallBackListener callBack);

        /*呼叫手机号*/
        void callMobileAccount(String mobileNo, CallBackListener callBack);

    }
}
