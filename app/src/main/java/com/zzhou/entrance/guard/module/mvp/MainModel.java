package com.zzhou.entrance.guard.module.mvp;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.body.UIProgressResponseCallBack;
import com.zhouyou.http.callback.DownloadProgressCallBack;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;
import com.zzhou.entrance.guard.AppConfig;
import com.zzhou.entrance.guard.Constants;
import com.zzhou.entrance.guard.MyApplication;
import com.zzhou.entrance.guard.bean.AccountData;
import com.zzhou.entrance.guard.bean.Ads;
import com.zzhou.entrance.guard.bean.HouseData;
import com.zzhou.entrance.guard.bean.ImeiNo;
import com.zzhou.entrance.guard.http.CallBackListener;
import com.zzhou.entrance.guard.sendNotify.SendNotify;
import com.zzhou.entrance.guard.source.CursorHelper;
import com.zzhou.entrance.guard.source.Ws;
import com.zzhou.entrance.guard.util.FileUtils;
import com.zzhou.entrance.guard.util.LogUtils;
import com.zzhou.entrance.guard.util.PackageUtils;
import com.zzhou.entrance.guard.util.ShellUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.netty.util.internal.StringUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 实现接口类-IMainContract.IModel
 * Created by The Moss on 2018/9/14.
 */

public class MainModel implements IMainContract.IModel {
    @Override
    public void getAdsInfo(final CallBackListener callBack) {
        LogUtils.d("start update ads info");
        EasyHttp.get(Constants.Api.SCREEN_ADS)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.d("update ads info >> error e = " + e.getMessage());
//                        callBack.onResult(false,e.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtils.d("update ads info >> success = " + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("code") == 0) {
                                List<Ads> adsList = Ads.fromJSONObject(json);
                                try {
                                    List<Ads> newList = checkAdsInfo(adsList);
                                    FileUtils.delAllFile(AppConfig.getInstance().APP_PATH_ROOT + "/ads");
                                    if (adsList != null && newList.size() > 0) {
                                        downFile(0, newList, callBack);
                                    }
                                } catch (Exception e) {
                                    LogUtils.d("update ads info >> load local file = " + e.getMessage());
                                }
                            }
                        } catch (JSONException e) {
                            LogUtils.d("update ads info >> exception e = " + e.getMessage());
                        }
                    }
                });
    }

    //上传抓拍照片并推送通知到微信公众号---高翔
    @Override
    public void uploadOpenInfo(int mode, String cardNo, File file, final CallBackListener callBack) {
        LogUtils.d("start upload open info mode = " + mode + "  cardNo = " + cardNo);
        EasyHttp.post(Constants.Api.UPLOAD_INFOS)
                .params("mode", String.valueOf(mode))
                .params("cardNo", cardNo)
//                .params("cardNo", "wx0001")
                .params("file", file, new UIProgressResponseCallBack() {
                    @Override
                    public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                        if (done) {
                            LogUtils.d("upload open info >> file upload complete");
                        }
                    }
                })
                .syncRequest(false)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.d("upload open info erro >>" + e.getMessage());
                        callBack.onResult(false, e.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtils.d("upload open info success >>" + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("code") == 0) {
                                callBack.onResult(true, "");
                            } else {
                                callBack.onResult(false, "");
                            }
                        } catch (JSONException e) {
                            LogUtils.d("upload open info exception >>" + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void updateVersion() {
        LogUtils.d("start update version");
        EasyHttp.get(Constants.Api.UPDATE)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.d("update version erro >>" + e.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtils.d("update version success >>" + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("code") == 0) {
                                // TODO: 2018/9/14 下载更新
                                if (!StringUtil.isNullOrEmpty(json.getString("data"))) {
                                    downApk(json.getString("data"));
                                }
                            }
                        } catch (JSONException e) {
                            LogUtils.d("update version exception >>" + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void uploadWarnInfo(int mode, CallBackListener callBack) {
        LogUtils.d("start upload warn info mode = " + mode);
        EasyHttp.post(Constants.Api.UPDATE_WARN)
                .params("mode", String.valueOf(mode))
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.d("upload warn info erro >>" + e.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtils.d("upload warn info success >>" + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("code") == 0) {
//                                callBack.onResult(true, "");
                            } else {
//                                callBack.onResult(false, "");
                            }
                        } catch (JSONException e) {
                            LogUtils.d("upload warn info erro >>" + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void getAccounts() {
        LogUtils.d("start update accounts");
        EasyHttp.get(Constants.Api.UPDATE_ACCOUNTS)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.e("update accounts error >> " + e.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtils.d("update accounts success >> " + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("code") == 0) {
                                List<AccountData> accounts = AccountData.fromJSONObject(json);
                                if (accounts != null && accounts.size() > 0) {//根据后台要求做全量更新，估先删除表内容
                                    int num = MyApplication.getInstance().getResovler().delete(Ws.AccountTable.CONTENT_URI, null, null);
                                    LogUtils.d("delete accounts >> success num = " + num);
                                }
                                for (int i = 0; i < accounts.size(); i++) {
                                    AccountData accountData = accounts.get(i);
                                    ContentValues values = new ContentValues();
                                    values.put(Ws.AccountTable.ACCOUNT_ID, accountData.getId());
                                    values.put(Ws.AccountTable.CARD, accountData.getCard());
                                    values.put(Ws.AccountTable.NO, accountData.getNo());
                                    values.put(Ws.AccountTable.PHONE, accountData.getPhone());
                                    values.put(Ws.AccountTable.JURISDICTION, accountData.getJurisdiction());
//                                    values.put("isAdd", accountData.getIsAdd());//根据后台要求做全量更新，估注释掉
                                    Uri uri = MyApplication.getInstance().getResovler().insert(Ws.AccountTable.CONTENT_URI, values);
                                    if (uri != null) {
                                        LogUtils.d("insert accounts >> success uri = " + uri);
                                    } else {
                                        LogUtils.d("insert accounts >> fail uri = null");
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            LogUtils.e("update accounts >> exception " + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void getHouses() {
        LogUtils.d("start update house");

        EasyHttp.get(Constants.Api.UPDATAE_HOUSER)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.e("update house error >> " + e.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtils.d("update house success >> " + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("code") == 0) {
                                List<HouseData> houses = HouseData.fromJSONObject(json);
                                if (houses != null && houses.size() > 0) {//根据后台要求做全量更新，故先删除表内容
                                    int num = MyApplication.getInstance().getResovler().delete(Ws.HouseTable.CONTENT_URI, null, null);
                                    LogUtils.d("delete house >> success num = " + num);
                                }

                                for (int i = 0; i < houses.size(); i++) {
                                    HouseData houseData = houses.get(i);
                                    ContentValues values = new ContentValues();
                                    values.put(Ws.HouseTable.ID, houseData.getId());
                                    values.put(Ws.HouseTable.NO, houseData.getNo());
                                    values.put(Ws.HouseTable.PHONE, houseData.getPhone());
                                    values.put(Ws.HouseTable.JURISDICTION, houseData.getJurisdiction());
//                                    values.put("isAdd", accountData.getIsAdd());//根据后台要求做全量更新，故注释掉
                                    Uri uri = MyApplication.getInstance().getResovler().insert(Ws.HouseTable.CONTENT_URI, values);
                                    if (uri != null) {
                                        LogUtils.d("insert house >> success uri = " + uri);
                                    } else {
                                        LogUtils.d("insert house >> fail uri = null");
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            LogUtils.e("update house >> exception " + e.getMessage());
                        }
                    }
                });
    }

    //验证卡号
    @Override
    public void validateCard(final String cardid, final CallBackListener callBack) {
        LogUtils.d("start validate card cardid = " + cardid);
        Observable observable = Observable.create(new ObservableOnSubscribe<AccountData>() {
            @Override
            public void subscribe(ObservableEmitter<AccountData> e) {
                try {
//                    Uri uri = Uri.parse(Ws.AccountTable.CONTENT_URI_CARD + "/" + cardid);//这种写法貌似只支持数字
                    Cursor cursor = MyApplication.getInstance().getResovler().query(Ws.AccountTable.CONTENT_URI, null,
                            Ws.AccountTable.CARD + "=?", new String[]{cardid}, null);
//                    Cursor cursor = MyApplication.getInstance().getResovler().query(Ws.AccountTable.CONTENT_URI, null,
//                            null, null, null);
                    if (cursor == null || cursor.getCount() < 1) {
                        String cardNo = Long.valueOf(cardid, 16) + "";
                        if (cardNo.length() % 2 != 0) {
                            cardNo = "0" + cardNo;
                        }
                        cursor = MyApplication.getInstance().getResovler().query(Ws.AccountTable.CONTENT_URI, null,
                                Ws.AccountTable.CARD + "=?", new String[]{cardNo}, null);
                    }
                    if (cursor == null) {
                        LogUtils.d("validate card >> error cursor is null ");
                        e.onError(new Throwable("validate card >> error cursor is null "));
                    } else {
                        LogUtils.d("validate card >> cursor size = " + cursor.getCount());
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            AccountData accounts = AccountData.fromCursor(new CursorHelper(cursor));
                            e.onNext(accounts);
                        } else {
                            e.onError(new Throwable("validate card >>  no account"));
                        }
                    }
                    e.onComplete();
                } catch (Exception e1) {
                    LogUtils.d("validate card excetion >> " + e1.getMessage());
                    e.onError(new Throwable(e1.getMessage()));
                }
            }
        }).subscribeOn(Schedulers.newThread());
        Disposable disposable = observable.subscribe(new Consumer<AccountData>() {
            @Override
            public void accept(AccountData datas) throws Exception {
                if (datas == null) {
                    callBack.onResult(false, null);
                } else {
//                            Toast.makeText(MyApplication.getInstance(),"accept",Toast.LENGTH_SHORT).show();
                    callBack.onResult(true, datas);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
//                        Toast.makeText(MyApplication.getInstance(),"onerro",Toast.LENGTH_SHORT).show();
                callBack.onResult(false, null);
            }
        });
    }

    @Override
    public void validatePass(String pass, final CallBackListener callBack) {
        LogUtils.d("start validate pass pass = " + pass);
        EasyHttp.get(Constants.Api.VALIDATE_PASS)
                .params("pwd", pass)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.d("validate pass >> error cursor is null ");
                        callBack.onResult(false, "");
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtils.d("validate card success >> " + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("code") == 0) {
                                callBack.onResult(true, json.getString("msg"));
                            } else {
                                callBack.onResult(false, "");
                            }
                        } catch (JSONException e) {
                            LogUtils.d("validate card excetion >> " + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void imeiAccount(final CallBackListener callBack) {
        LogUtils.d("start imeiAccount");
        EasyHttp.get(Constants.Api.TEL)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtils.e("imeiAccount error >> " + e.getMessage());
                        callBack.onResult(false, "");
                    }

                    @Override
                    public void onSuccess(String s) {
                        LogUtils.d("imeiAccount s >>" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (json.getInt("code") == 0) {
                                JSONObject obj = json.getJSONObject("data");
                                ImeiNo imei = new ImeiNo();
                                imei.setBoardNumber(obj.getString("zj"));
                                imei.setExtensionNumber(obj.getString("fj"));
                                imei.setPass(obj.getString("pw"));
                                callBack.onResult(true, imei);
                            } else {
                                callBack.onResult(false, "");
                            }
                        } catch (JSONException e) {
                            callBack.onResult(false, "获取呼叫中心账号错误");
                            LogUtils.d("imeiAccount json erro  >>" + e.getMessage());
                        }
                    }
                });

    }

    /**
     * 从本地数据库中获取房间绑定的手机号
     * @param houseNo
     * @param callBack
     */
    @Override
    public void callAccount(final String houseNo, final CallBackListener callBack) {
        LogUtils.d("呼叫流程-1：start callAccount houseNo = " + houseNo);
        Observable observable = Observable.create(new ObservableOnSubscribe<HouseData>() {
            @Override
            public void subscribe(ObservableEmitter<HouseData> e) {
                try {
                    Cursor cursor = MyApplication.getInstance().getResovler().query(Ws.HouseTable.CONTENT_URI, null,
                            Ws.HouseTable.NO + "=?", new String[]{houseNo}, null);
//                    Cursor cursor = MyApplication.getInstance().getResovler().query(Ws.AccountTable.CONTENT_URI, null,
//                            null, null, null);
                    if (cursor == null) {
                        LogUtils.d("callAccount >> error cursor is null ");
                        e.onError(new Throwable("callAccount >> error cursor is null "));
                    } else {
                        LogUtils.d("callAccount >> cursor size = " + cursor.getCount());
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            HouseData house = HouseData.fromCursor(new CursorHelper(cursor));
                            e.onNext(house);
                        } else {
                            e.onError(new Throwable("callAccount >>  no account"));
                        }
                    }
                    e.onComplete();
                } catch (Exception e1) {
                    LogUtils.d("callAccount excetion >> " + e1.getMessage());
                    e.onError(new Throwable(e1.getMessage()));
                }
            }
        }).subscribeOn(Schedulers.newThread());
        Disposable disposable = observable.subscribe(new Consumer<HouseData>() {
            @Override
            public void accept(HouseData datas) throws Exception {
                if (datas == null) {
                    callBack.onResult(false, null);
                } else {
//                            Toast.makeText(MyApplication.getInstance(),"accept",Toast.LENGTH_SHORT).show();
                    callBack.onResult(true, datas);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
//                        Toast.makeText(MyApplication.getInstance(),"onerro",Toast.LENGTH_SHORT).show();
                callBack.onResult(false, null);
            }
        });
    }

    /**
     * 与本地数据库中房间绑定的手机号进行匹配
     * @param mobileNo
     * @param callBack
     */
    @Override
    public void callMobileAccount(final String mobileNo, final CallBackListener callBack) {
        LogUtils.d("呼叫流程step2_model_callMobileAccount：start callMobileAccount mobileNo = " + mobileNo);
        Observable observable = Observable.create(new ObservableOnSubscribe<HouseData>() {
            @Override
            public void subscribe(ObservableEmitter<HouseData> e) {
                try {
                    Cursor cursor = MyApplication.getInstance().getResovler().query(Ws.HouseTable.CONTENT_URI, null,
                            Ws.HouseTable.PHONE + "=?", new String[]{mobileNo}, null);
//                    Cursor cursor = MyApplication.getInstance().getResovler().query(Ws.AccountTable.CONTENT_URI, null,
//                            null, null, null);
                    if (cursor == null) {
                        LogUtils.d("callMobileAccount >> error cursor is null ");
                        e.onError(new Throwable("callMobileAccount >> error cursor is null "));
                    } else {
                        LogUtils.d("callMobileAccount >> cursor size = " + cursor.getCount());
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            HouseData house = HouseData.fromCursor(new CursorHelper(cursor));
                            e.onNext(house);
                        } else {
                            e.onError(new Throwable("callMobileAccount >>  no mobile account >>>>>>>>>>>>>>>>>>>>>>>>>>"));
                        }
                    }
                    e.onComplete();
                } catch (Exception e1) {
                    LogUtils.d("callMobileAccount excetion >> " + e1.getMessage());
                    e.onError(new Throwable(e1.getMessage()));
                }
            }
        }).subscribeOn(Schedulers.newThread());
        Disposable disposable = observable.subscribe(new Consumer<HouseData>() {
            @Override
            public void accept(HouseData datas) throws Exception {
                if (datas == null) {
                    callBack.onResult(false, null);
                } else {
//                            Toast.makeText(MyApplication.getInstance(),"accept",Toast.LENGTH_SHORT).show();
                    callBack.onResult(true, datas);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
//                        Toast.makeText(MyApplication.getInstance(),"onerro",Toast.LENGTH_SHORT).show();
                callBack.onResult(false, null);
            }
        });
    }

    //下载广告文件
    private void downFile(int index, final List<Ads> adslist, final CallBackListener listener) {
        final int post = index;
        final Ads ads = adslist.get(post);
        String url = ads.getUrl();
        String imgName = url.substring(url.lastIndexOf("/") + 1, url.length());
        EasyHttp.downLoad(url)
                .saveName(imgName)
                .savePath(AppConfig.getInstance().APP_PATH_ROOT + "/ads")
                .execute(new DownloadProgressCallBack<String>() {
                    @Override
                    public void update(long bytesRead, long contentLength, boolean done) {

                    }

                    @Override
                    public void onComplete(String path) {
                        ads.setUrl(path);
                        if (post < adslist.size() - 1) {
                            downFile(post + 1, adslist, listener);
                        } else {
                            listener.onResult(true, adslist);
                        }
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onError(ApiException e) {
                        LogUtils.e("down load error " + e.getMessage());
                    }
                });
    }

    /**
     * 下载apk
     *
     * @param url
     */
    private void downApk(String url) {
        LogUtils.d("down apk url " + url);
        String imgName = url.substring(url.lastIndexOf("/") + 1, url.length());
        EasyHttp.downLoad(url)
                .saveName(imgName)
                .savePath(AppConfig.getInstance().APP_PATH_ROOT)
                .execute(new DownloadProgressCallBack<String>() {
                    @Override
                    public void update(long bytesRead, long contentLength, boolean done) {
                        if (done) {
                            LogUtils.d("down apk >> complete done");
                        }
                    }

                    @Override
                    public void onComplete(String path) {
                        LogUtils.d("down apk complete file path = " + path);
                        if (path == null || !new File(path).exists()) {
                            return;
                        }
                        install(path);
//                        // TODO: 2018/9/14 启动静默安装启动 需要root权限
//                        boolean isSuccess = ApkController.install(path, MyApplication.getInstance());
//                        if (isSuccess) {
//                            LogUtils.d("install is success start luncher app ");
//                            ApkController.startApp("com.zzhou.entrance.guard", "com.zzhou.entrance.guard.module.MainActivity");
//                        } else {
//                            LogUtils.e("install is fail");
//                        }
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onError(ApiException e) {
                        LogUtils.d("down apk >> erro e " + e.getMessage());
                    }
                });
    }

    private List<Ads> checkAdsInfo(List<Ads> adsList) {
        List<Ads> list = new ArrayList();
        for (Ads ads : adsList) {
            if (ads.getType() == 1) {//1视频，如果有视频，跳出循环，删除其他数据，保留第一个视频数据
                list.clear();
                list.add(ads);
                break;
            }
            if (list.size() <= 10) {//这里存放的全是图片，当超过10张其他就舍弃
                list.add(ads);
            }
        }
        return list;
    }

    private void install(String apkPath) {
        boolean b = ShellUtils.checkRootPermission();
        if (b) {
//            String apkPath = apkFile.getAbsolutePath();
            int resultCode = PackageUtils.installSilent(MyApplication.getInstance(), apkPath);
            if (resultCode != PackageUtils.INSTALL_SUCCEEDED) {
//                Toast.makeText(this, "升级失败",     Toast.LENGTH_SHORT).show();
                LogUtils.e("升级失败");
            } else {
                FileUtils.deleteFile(apkPath);
                LogUtils.e("升级成功");
            }
//            ApkController.copy2SystemApp(apkPath);//设置系统进程
        }
    }
}