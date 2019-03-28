package com.zzhou.entrance.guard.module.mvp;

import com.zzhou.entrance.guard.AppConfig;
import com.zzhou.entrance.guard.MyApplication;
import com.zzhou.entrance.guard.bean.AccountData;
import com.zzhou.entrance.guard.bean.Ads;
import com.zzhou.entrance.guard.bean.HouseData;
import com.zzhou.entrance.guard.bean.ImeiNo;
import com.zzhou.entrance.guard.http.CallBackListener;
import com.zzhou.entrance.guard.util.DynamicPwd;
import com.zzhou.entrance.guard.util.FileUtils;
import com.zzhou.entrance.guard.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.netty.util.internal.StringUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 实现接口类-IMainContract.IPresenter
 * Created by The Moss on 2018/9/16.
 */

public class MainPresenter implements IMainContract.IPresenter {
    IMainContract.IView iView;
    IMainContract.IModel iModel;
    protected String TAG;

    public MainPresenter(IMainContract.IView iView) {
        this.iView = iView;
        iModel = new MainModel();
    }

    @Override
    public void getAdsInfo() {
        iModel.getAdsInfo(new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                try {
                    FileUtils.saveObject((List<Ads>) result, AppConfig.getInstance().APP_PATH_ROOT + "/ads/AdsBean");
                } catch (IOException e) {
                    LogUtils.e("save object exception " + e.getMessage());
                }
                iView.channelAdsResult(isSuccess, (List<Ads>) result);
            }
        });
    }

    @Override
    public void getLocalAdsInfo() {
        Observable.create(new ObservableOnSubscribe<List<Ads>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Ads>> e) throws Exception {
                Object object = FileUtils.loadObject(AppConfig.getInstance().APP_PATH_ROOT + "/ads/AdsBean");
                if (object != null && object instanceof List) {
                    try {
                        List<Ads> adsList = (List<Ads>) object;
                        e.onNext(adsList);
                    } catch (Exception e1) {
                        LogUtils.e("load object exception " + e1.getMessage());
                        e.onError(null);
                    }
                } else {
                    LogUtils.d("load object == null && object not instanceof List");
                    e.onError(null);
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Ads>>() {
                    @Override
                    public void accept(List<Ads> ads) throws Exception {
                        iView.channelAdsResult(true, ads);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getAdsInfo();
                    }
                });
    }

    @Override
    public void validateCard(String cardid) {
//        iView.validateAccountResult(true, new AccountData());
        iModel.validateCard(cardid, new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                try {
                    if (isSuccess) {
                        iView.validateAccountResult(true, (AccountData) result);

                    } else {
                        iView.validateAccountResult(false, null);
                    }
                } catch (Exception e) {
                    iView.validateAccountResult(false, null);
                }
            }
        });
    }

    @Override
    public void uploadOpenInfo(int mode, String cardNo, final File file) {
        iModel.uploadOpenInfo(mode, cardNo, file, new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                if (isSuccess) {
                    FileUtils.deleteFile(file);
                    LogUtils.d("upload open info delete file");
                }
            }
        });
    }

    @Override
    public void updateVersion() {
        iModel.updateVersion();
    }

    @Override
    public void uploadWarnInfo(final int mode) {
        iModel.uploadWarnInfo(mode, new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                try {
                    if (!isSuccess) {
                        LogUtils.e("uploadWarnInfo fail reUpload delay 5s");
                        Thread.sleep(5 * 1000);
                        uploadWarnInfo(mode);
                    }
                } catch (InterruptedException e) {
                    LogUtils.e("uploadWarnInfo exception " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void getAccounts() {
        iModel.getAccounts();
    }

    @Override
    public void getHouses() {
        iModel.getHouses();
    }

    @Override
    public void validatePass(String pass) {
//        iView.validatePass(true);
        if (StringUtil.isNullOrEmpty(pass)) {
            iView.validatePass(false, "密码为空！");
            return;
        }
        if (pass.equals(DynamicPwd.createPwd(MyApplication.getInstance().getDeviceNo(), 0)) ||
                pass.equals(DynamicPwd.createPwd(MyApplication.getInstance().getDeviceNo(), 5)) ||
                pass.endsWith(DynamicPwd.createPwd(MyApplication.getInstance().getDeviceNo(), -5))) {
            iView.validatePass(true, "临时密码");
            return;
        }
        iModel.validatePass(pass, new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                iView.validatePass(isSuccess, result.toString());
            }
        });
    }

    @Override
    public void imeiAccount() {
//        ImeiNo no = new ImeiNo();
//        no.setBoardNumber("sz888.mixcaller.com:7080");
//        no.setExtensionNumber("58402");
//        no.setPass("mixcaller");
//        iView.imeiAccountResult(true, no, "");
        iModel.imeiAccount(new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                if (isSuccess) {
                    iView.imeiAccountResult(isSuccess, (ImeiNo) result,"");
                }else{
                    iView.imeiAccountResult(isSuccess, null,result.toString());
                }
            }
        });
    }

    /**
     *实现接口IMainContract.IPresenter-callAccount
     * 呼叫房间
     * @param houseNo
     */
    @Override
    public void callAccount(String houseNo) {
//        iView.callAccountResult(true, "15555155506",1);
        iModel.callAccount(houseNo, new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                if (isSuccess) {
                    HouseData house = (HouseData) result;
                    if (house == null || StringUtil.isNullOrEmpty(house.getPhone())) {
                        iView.callAccountResult(false, "",0);
                    } else {
                        iView.callAccountResult(true, house.getPhone(),house.getJurisdiction());
                    }
                } else {
                    iView.callAccountResult(false, "",0);
                }

            }
        });
    }

    /**
     * 实现接口IMainContract.IPresenter-callMobileAccount
     * 呼叫手机号
     * @param mobileNo
     */
    @Override
    public void callMobileAccount(String mobileNo) {
        LogUtils.d(TAG,"呼叫手机号流程step1_Presenter_callMobileAccount："+mobileNo);
        iModel.callMobileAccount(mobileNo, new CallBackListener() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                if (isSuccess) {
                    HouseData house = (HouseData) result;
                    if (house == null || StringUtil.isNullOrEmpty(house.getPhone())) {
                        iView.callMobileAccountResult(false, "",0);
                    } else {
                        iView.callMobileAccountResult(true, house.getPhone(),house.getJurisdiction());
                    }
                } else {
                    iView.callMobileAccountResult(false, "",0);
                }

            }
        });
    }
}
