//package com.zzhou.entrance.guard.module;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.ImageFormat;
//import android.graphics.Matrix;
//import android.graphics.Rect;
//import android.graphics.YuvImage;
//import android.hardware.Camera;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.SoundPool;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.annotation.Nullable;
//import android.support.annotation.StringRes;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.view.KeyEvent;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.emicnet.emicallapi.voip.CallStateListener;
//import com.emicnet.emicallapi.voip.EMICallAgent;
//import com.emicnet.emicallapi.voip.InComingCallLauncherListener;
//import com.emicnet.emicallapi.web.Callback;
//import com.wang.avi.AVLoadingIndicatorView;
//import com.zhouyou.http.utils.Utils;
//import com.zzhou.entrance.guard.AppConfig;
//import com.zzhou.entrance.guard.MyApplication;
//import com.zzhou.entrance.guard.R;
//import com.zzhou.entrance.guard.acs.CameraPreview;
//import com.zzhou.entrance.guard.acs.GpioManager;
//import com.zzhou.entrance.guard.bean.AccountData;
//import com.zzhou.entrance.guard.bean.Ads;
//import com.zzhou.entrance.guard.bean.ImeiNo;
//import com.zzhou.entrance.guard.module.mvp.IMainContract;
//import com.zzhou.entrance.guard.module.mvp.MainPresenter;
//import com.zzhou.entrance.guard.netty.activity.NettyActivity;
//import com.zzhou.entrance.guard.netty.bean.MessageInfo;
//import com.zzhou.entrance.guard.netty.bean.RequestCmd;
//import com.zzhou.entrance.guard.util.FileUtils;
//import com.zzhou.entrance.guard.util.LogUtils;
//import com.zzhou.entrance.guard.util.ToastShow;
//import com.zzhou.entrance.guard.widget.CustomerVideoView;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import id.zelory.compressor.Compressor;
//import io.netty.util.internal.StringUtil;
//import io.reactivex.functions.Consumer;
//import io.reactivex.schedulers.Schedulers;
//
////import com.zzhou.entrance.guard.widget.CustomDialog;
//
///**
// * 屏保页面，无操作时显示界面，后台处理相关网络请求，IM消息
// * Created by The Moss on 2018/8/31.
// * http://js.emic.com.cn:1046/
// * admin_02566695505  admin02566695505
// */
//public class MainActivity1 extends NettyActivity implements IMainContract.IView, GpioManager.GpioListener, InComingCallLauncherListener {
//    ImageView mAdsImage;
//    CustomerVideoView mVideoView;
//    LinearLayout mDialogLl;
//    TextView mContent;
//    AVLoadingIndicatorView mAvi;
//
////    private CameraPreview mySurfaceView;
//
//    FragmentManager mManager;
//    FragmentTransaction mTransaction;
////    static MainActivity instence;
//
//    IMainContract.IPresenter iPresenter;
//    GpioManager mGpioManager;
//    StringBuffer inputStr;
//
//    final static int INPUT_OPEN = 0;
//    final static int INPUT_CALL = 1;
//    final static int INPUT_ERROR = 2;
//
//    static final int MODE_CARD = 0;
//    static final int MODE_WXPASS = 1;
//    static final int MODE_REMOTE = 2;
//    int mode = MODE_CARD;
//
//    List<Ads> imslist = new ArrayList<>();
//    int time;
//    String cardNo = "";
//    private boolean isOnLongPass = false;//长按#
//    private boolean isShowMac = false;//是否正在显示mac地址
//    boolean isOpenDooring = false;//是否正常开门
//
//    private SoundPool spPool;
//    int sound;
//
//    @SuppressLint("HandlerLeak")
//    Handler imageHandler = new Handler();
//    Runnable imageRun = new Runnable() {
//        @Override
//        public void run() {
//            if (imslist == null || imslist.size() < 1) {
//                return;
//            }
//            if (time == imslist.size()) {
//                time = 0;
//            }
//            String url = imslist.get(time % imslist.size()).getUrl();
//            if (Utils.checkMain()) {
//                Glide.with(MainActivity1.this)
//                        .load(url)
//                        .into(mAdsImage);
//            }
//            if (imslist.size() > 1) {//超过1张图片，则延迟4秒后切换下一张
//                time++;
//                imageHandler.postDelayed(imageRun, 4000);
//            }
//        }
//    };
//    private Handler longPassHanlder = new Handler();
//
//    Runnable longRun = new Runnable() {
//        @Override
//        public void run() {
//            if (isOnLongPass) {
//                isShowMac = true;
//                inputStr.delete(0, inputStr.length());
//                showDialog(false, MyApplication.getInstance().getDeviceNo());
//            }
//        }
//    };
//    //无操作倒计时
//    Handler handler = new Handler();
//    private Runnable timeRun = new Runnable() {
//        @Override
//        public void run() {
//            hideDialog();
//        }
//    };
//    //camera preview
//    private Camera mCamera;
//    private CameraPreview mCameraPreviewSurface;
//    private boolean isGetPreview = false;
//    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
//        public void onPreviewFrame(byte[] data, Camera camera) {
//            takePhotoPreview(data, camera);
//        }
//    };
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // 避免从桌面启动程序后，会重新实例化入口类的activity
//        if (!this.isTaskRoot()) { // 判断当前activity是不是所在任务栈的根
//            Intent intent = getIntent();
//            if (intent != null) {
//                String action = intent.getAction();
//                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
//                    finish();
//                    return;
//                }
//            }
//        }
//        setContentView(R.layout.activity_main);
////        mySurfaceView = findViewById(R.id.activity_camera_surfaceview);
//        mAdsImage = findViewById(R.id.activity_screen_image);
//        mVideoView = findViewById(R.id.activity_screen_video);
//        mDialogLl = findViewById(R.id.dialog_Ll);
//        mContent = findViewById(R.id.dialog_content);
//        mAvi = findViewById(R.id.dialog_avi);
//
//        mManager = getSupportFragmentManager();
//        mTransaction = mManager.beginTransaction();
//        iPresenter = new MainPresenter(this);
//        inputStr = new StringBuffer();
//
//        initAudioRaw();//加载音频资源
//        mGpioManager = GpioManager.getInstance(this);//初始化GPI
////        EMICallAgent.getInstance().setOnInComingCallLauncherListener(this);//呼叫中心来电监听
////        EMICallAgent.getInstance().setOnCallStateListener(callStateListener);//通话状态监听
////        mAdsImage.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                iPresenter.getAdsInfo();
////                readCardlistener("wx0001");
////                iPresenter.validateCard("04BB1D22EF4A80");
////            }
////        });
////        showDialog(true, R.string.app_install_ing);
//        iPresenter.getAccounts();
//        iPresenter.getLocalAdsInfo();
////        iPresenter.imeiAccount();
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        mySurfaceView = findViewById(R.id.activity_camera_surfaceview);
////        mySurfaceView.init();
//        isOnLongPass = false;
//        mGpioManager.onResume();
//        imageHandler.removeCallbacks(imageRun);
//        if (imslist != null || imslist.size() > 0) {
//            imageHandler.postDelayed(imageRun, 4000);
//        }
//        new Handler(getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                mCamera = getCameraInstance();
//                mCameraPreviewSurface = new CameraPreview(MainActivity1.this, mCamera, previewCb);
//                FrameLayout preview = findViewById(R.id.camera);
//                preview.addView(mCameraPreviewSurface);
//            }
//        });
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
////        mySurfaceView = findViewById(R.id.activity_camera_surfaceview);
////        mySurfaceView.onPause();
//        mGpioManager.onPause();
//        if (Utils.checkMain()) {
//            Glide.with(this).pauseRequests();
//            imageHandler.removeCallbacks(imageRun);
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        releaseCamera();
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (mVideoView!= null && mVideoView.isPlaying()) {
//            mVideoView.stopPlayback();
//        }
//        if (spPool != null) {
//            spPool.stop(sound);
//            spPool.release();
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    public void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        mGpioManager.onNewIntent(intent);
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (isOpenDooring) {
//            return true;
//        }
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_1:
////                ToastShow.show(this, "onKeyDown KEYCODE_1");
//                inputStr.append("\t1");
//                break;
//            case KeyEvent.KEYCODE_2:
////                ToastShow.show(this, "onKeyDown KEYCODE_2");
//                inputStr.append("\t2");
//                break;
//            case KeyEvent.KEYCODE_3:
////                ToastShow.show(this, "onKeyDown KEYCODE_3");
//                inputStr.append("\t3");
//                break;
//            case KeyEvent.KEYCODE_4:
////                ToastShow.show(this, "onKeyDown KEYCODE_4");
//                inputStr.append("\t4");
//                break;
//            case KeyEvent.KEYCODE_5:
////                ToastShow.show(this, "onKeyDown KEYCODE_5");
//                inputStr.append("\t5");
//                break;
//            case KeyEvent.KEYCODE_6:
////                ToastShow.show(this, "onKeyDown KEYCODE_6");
//                inputStr.append("\t6");
//                break;
//            case KeyEvent.KEYCODE_7:
////                ToastShow.show(this, "onKeyDown KEYCODE_7");
//                inputStr.append("\t7");
//                break;
//            case KeyEvent.KEYCODE_8:
////                ToastShow.show(this, "onKeyDown KEYCODE_8");
//                inputStr.append("\t8");
//                break;
//            case KeyEvent.KEYCODE_9:
////                ToastShow.show(this, "onKeyDown KEYCODE_9");
//                inputStr.append("\t9");
//                break;
//            case KeyEvent.KEYCODE_0:
////                ToastShow.show(this, "onKeyDown KEYCODE_0");
//                inputStr.append("\t0");
//                break;
//            case KeyEvent.KEYCODE_STAR:
////                ToastShow.show(this, "onKeyDown KEYCODE_STAR");
//                break;
//            case KeyEvent.KEYCODE_POUND:
////                LogUtils.d(TAG, "onKeyDown KEYCODE_POUND");
////                if (inputStr.length() > 0) {
////                    isOnLongPass = false;
////                    break;
////                }
//                if (!isOnLongPass) {
//                    inputStr.append("\t#");
//                    isOnLongPass = true;
//                    longPassHanlder.postDelayed(longRun, 3000);
//                }
//                break;
//
//            //detect io1-4, F1-F4
//            case KeyEvent.KEYCODE_F1:
////                ToastShow.show(this, "onKeyDown KEYCODE_F1");
//                return mGpioManager.onMagneticOffKey(keyCode, event);//门磁关
//            case KeyEvent.KEYCODE_F2:
////                ToastShow.show(this, "onKeyDown KEYCODE_F2");
//                return mGpioManager.onMagneticOffKey(keyCode, event);
//            case KeyEvent.KEYCODE_F3:
//                return mGpioManager.onMagneticOffKey(keyCode, event);
//            case KeyEvent.KEYCODE_F4:
//                return mGpioManager.onMagneticOffKey(keyCode, event);
//            default:
//                break;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
////        LogUtils.d(TAG, "onKeyUp keycode " + keyCode);
//        if (isOpenDooring) {
//            ToastShow.show(this, "正在开门，请稍后....");
//            return true;
//        }
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_1:
////                ToastShow.show(this, "onKeyUp KEYCODE_1");
//                break;
//            case KeyEvent.KEYCODE_2:
////                ToastShow.show(this, "onKeyUp KEYCODE_2");
//                break;
//            case KeyEvent.KEYCODE_3:
////                ToastShow.show(this, "onKeyUp KEYCODE_3");
//                break;
//            case KeyEvent.KEYCODE_4:
////                ToastShow.show(this, "onKeyUp KEYCODE_4");
//                break;
//            case KeyEvent.KEYCODE_5:
////                ToastShow.show(this, "onKeyUp KEYCODE_5");
//                break;
//            case KeyEvent.KEYCODE_6:
////                ToastShow.show(this, "onKeyUp KEYCODE_6");
//                break;
//            case KeyEvent.KEYCODE_7:
////                ToastShow.show(this, "onKeyUp KEYCODE_7");
//                break;
//            case KeyEvent.KEYCODE_8:
////                ToastShow.show(this, "onKeyUp KEYCODE_8");
//                break;
//            case KeyEvent.KEYCODE_9:
////                ToastShow.show(this, "onKeyUp KEYCODE_9");
//                break;
//            case KeyEvent.KEYCODE_0:
////                ToastShow.show(this, "onKeyUp KEYCODE_0");
//                break;
//            case KeyEvent.KEYCODE_STAR:
////                ToastShow.show(this, "onKeyUp KEYCODE_STAR");
//                hideDialog();
//                return true;
//            case KeyEvent.KEYCODE_POUND:
////                LogUtils.d(TAG, "onKeyUp KEYCODE_POUND");
//                isOnLongPass = false;
//                longPassHanlder.removeCallbacks(longRun);
//                if (isShowMac) {
//                    isShowMac = false;
//                    return true;
//                }
//                break;
//
//            //detect io1-4, F1-F4
//            case KeyEvent.KEYCODE_F1:
//                hideDialog();
//                return mGpioManager.onMagneticOnKey(keyCode, event);
//            case KeyEvent.KEYCODE_F2:
//                hideDialog();
//                return mGpioManager.onMagneticOnKey(keyCode, event);
//            case KeyEvent.KEYCODE_F3:
//                return mGpioManager.onMagneticOnKey(keyCode, event);
//            case KeyEvent.KEYCODE_F4:
//                return mGpioManager.onMagneticOnKey(keyCode, event);
//            default:
//                break;
//        }
//        showDialog(false, inputStr.toString());
//        int format = formartInput(inputStr.toString().replaceAll("\t", ""));
////        Toast.makeText(this, "format = " + format, Toast.LENGTH_SHORT).show();
//        switch (format) {
//            case INPUT_OPEN:
//                handler.removeCallbacks(timeRun);//正在验证账号去掉倒计时
////                openStips("正在开门，请稍后！");
//                if (isOpenDooring) {
//                    showDialog(false, R.string.on_reopening_door_tips);
//                    return true;
//                }
//                showDialog(true, R.string.on_opening_door_tips);
//                isOpenDooring = true;
//                // TODO: 2018/9/20  后台验证密码是否正确
//                iPresenter.validatePass(inputStr.toString().replaceFirst("#", "")
//                        .replaceAll("\t", ""));
//                return true;
//            case INPUT_CALL:
//                handler.removeCallbacks(timeRun);
//                showDialog(true, R.string.on_calling_tips);
//                // TODO: 2018/9/20 呼叫中心接口
//                iPresenter.callAccount(inputStr.toString().replaceFirst("#", "")
//                        .replaceAll("\t", ""));
//                inputStr.delete(0, inputStr.length());
//                return true;
//            case INPUT_ERROR:
//                hideDialog();
//                return true;
//            default:
//        }
//        startNoTouch();//重置无操作倒计时
//        return super.onKeyUp(keyCode, event);
//    }
//
//    @Override
//    protected void notifyData(MessageInfo message) {
//        int cmd = message.getCmd();
//        switch (cmd) {
//            case RequestCmd.OPEN_DOOR:
//                // TODO: 2018/9/14 远程开门
////                Toast.makeText(this, "******************远程开门*******************", Toast.LENGTH_SHORT).show();
//                LogUtils.d("******************远程开门*******************");
//                showDialog(true, R.string.on_opening_door_remeta_warn);
//                isOpenDooring = true;
//                mode = MODE_REMOTE;
//                cardNo = message.getContent();
//                openDoor(0);
//                break;
//            case RequestCmd.UPDATE_ADS:
//                // TODO: 2018/9/14 更新广告
////                Toast.makeText(this, "******************更新广告*******************", Toast.LENGTH_SHORT).show();
//                LogUtils.d("******************更新广告*******************");
//                iPresenter.getAdsInfo();
//                break;
//            case RequestCmd.UPDATE_ACCOUNTS:
//                // TODO: 2018/9/14 更新用户数据
////                Toast.makeText(this, "******************更新用户数据*******************", Toast.LENGTH_SHORT).show();
//                LogUtils.d("******************更新用户数据*******************");
//                iPresenter.getAccounts();
//                break;
//            case RequestCmd.UPDATE_VERSION:
//                // TODO: 2018/9/14 更新版本
////                Toast.makeText(this, "******************更新版本*******************", Toast.LENGTH_SHORT).show();
//                LogUtils.d("******************更新版本*******************");
//                iPresenter.updateVersion();
//                break;
//        }
//    }
//
//    @Override
//    public void channelAdsResult(boolean isSuccess, List<Ads> adslsit) {
//        if (isSuccess) {
//            Ads ads = adslsit.get(0);//视频只有一条，图片可以多条，这里简单处理下，判断第一个数据是视频还是图片
//            LogUtils.d("ads type = " + ads.getType() + " url = " + ads.getUrl());
//            if (ads.getType() == 0) {
//                if (mVideoView.isPlaying()) {
//                    mVideoView.stopPlayback();
//                }
//                imslist.clear();
//                imslist.addAll(adslsit);
//                mVideoView.setVisibility(View.GONE);
//                mAdsImage.setVisibility(View.VISIBLE);
//                imageHandler.post(imageRun);
//            } else {
////                String url = "http://jzvd.nathen.cn/63f3f73712544394be981d9e4f56b612/69c5767bb9e54156b5b60a1b6edeb3b5-5287d2089db37e62345123a1be272f8b.mp4";
//                imageHandler.removeCallbacks(imageRun);
//                String url = "";
//                if (!StringUtil.isNullOrEmpty(ads.getUrl())) {
//                    url = ads.getUrl();
//                }
//                if (StringUtil.isNullOrEmpty(url) || !url.endsWith(".mp4")) {
//                    return;
//                }
//                if (FileUtils.fileExists(url)) {
//                    mVideoView.setVisibility(View.VISIBLE);
//                    mAdsImage.setVisibility(View.GONE);
//                    playVideo(url);
//                } else {
//                    LogUtils.e("video path not exist, may be by deleted");
//                }
//            }
//        }
//    }
//
//    @Override
//    public void validateAccountResult(boolean isSuccess, AccountData account) {
//        if (isSuccess) {
//            mode = MODE_CARD;
//            if (account != null) {
//                cardNo = account.getCard();
//                openDoor(account.getJurisdiction());
//            } else {
//                showDialog(false, R.string.open_door_fail);
//                isOpenDooring = false;
//                startNoTouch();
//            }
//        } else {
//            showDialog(false, R.string.open_door_fail);
//            isOpenDooring = false;
//            startNoTouch();
//        }
////        openStips("");
//    }
//
//    @Override
//    public void imeiAccountResult(boolean isSuccess, ImeiNo imei, String msg) {
//        hideDialog();
//        if (isSuccess && imei != null) {
//            registEMICall(imei.getBoardNumber(), imei.getExtensionNumber(), imei.getPass());
//        }
//    }
//
//    @Override
//    public void callAccountResult(boolean isSuccess, String msg) {
//        if (!isSuccess) {
////            CustomDialog.create(instence).setContent(R.string.call_fail).hideAvi();
//            showDialog(false, R.string.call_fail);
//            startNoTouch();
//        } else {
//            showDialog(true, R.string.on_calling_tips);
//            startNoTouch();
//        }
//    }
//
//    @Override
//    public void validatePass(boolean isSuccess,String msg) {
//        inputStr.delete(0, inputStr.length());
//        if (isSuccess) {
//            mode = MODE_WXPASS;
//            openDoor(0);
//        } else {
//            showDialog(false, R.string.open_door_passfail);
//            isOpenDooring = false;
//            startNoTouch();
//        }
////        openStips("");
//    }
//
//    @Override
//    public void readCardlistener(String cardid) {
//        Toast.makeText(this, "刷卡开门" + cardid, Toast.LENGTH_SHORT).show();
//        if (isOpenDooring) {
//            showDialog(false, R.string.on_reopening_door_tips);
//            return;
//        }
//        // 验证卡号
//        showDialog(true, R.string.on_opening_door_tips);
//        isOpenDooring = true;
//        cardid = cardid.replaceAll(" ", "");
//        iPresenter.validateCard(cardid);
//    }
//
//    @Override
//    public void overTimelistener() {
//        iPresenter.uploadWarnInfo(0);
//    }
//
//    @Override
//    public void onTemperatureWarnListener() {
//        iPresenter.uploadWarnInfo(1);
//    }
//
//    @Override
//    public void onInComingCallLauncher(String s) {
//        LogUtils.d("电话呼入监听 " + s);
//    }
//
//    /**
//     * 注册呼叫中心
//     */
//    private void registEMICall(final String switchboardNumber, final String extensionNumber, final String password) {
//        LogUtils.d("go regist EMICall");
//        EMICallAgent.getInstance().login(switchboardNumber, extensionNumber, password, new Callback<String>() {
//            @Override
//            public void onSuccess(String s) {
//                LogUtils.d("regist EMICall success " + s);
//            }
//
//            @Override
//            public void onFailture(int i, String s) {
//                LogUtils.d("regist EMICall Fail " + s + " i = " + i);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        registEMICall(switchboardNumber, extensionNumber, password);
//                    }
//                }, 3000);
//            }
//        });
//    }
//
//    /**
//     * @param which 开哪个门
//     */
//    private void openDoor(int which) {
//        isOpenDooring = false;
//        showDialog(false, R.string.open_door_success);
//        switch (mode) {
//            case MODE_CARD:
//                if (which == 0) {
//                    mGpioManager.openDoor(true);
//                } else if (which == 1) {
//                    mGpioManager.openSecondDoor(true);
//                } else {
//                    mGpioManager.openDoor(true);
//                    mGpioManager.openSecondDoor(true);
//                }
//                break;
//            case MODE_WXPASS:
//                mGpioManager.openDoor(true);
//                break;
//            case MODE_REMOTE:
//                mGpioManager.openDoor(true);
//                break;
//        }
//        isGetPreview = true;
//        new Thread() {
//            @Override
//            public void run() {
//                spPool.play(sound, 1, 1, 1, 0, 1);
//            }
//        }.start();
//    }
//
//    private void playVideo(final String url) {
//        if (mVideoView.isPlaying()) {
//            mVideoView.stopPlayback();
//        }
//        mVideoView.setVisibility(View.VISIBLE);
//        mVideoView.setVideoPath(url);
//        mVideoView.start();
//        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mp.setVolume(0f, 0f);
//                mp.start();
//                mp.setLooping(true);
//            }
//        });
////        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
////            @Override
////            public void onCompletion(MediaPlayer mp) {
////                mp.reset();
////                mVideoView.setVideoPath(url);
////                mVideoView.start();
////            }
////        });
//        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                mVideoView.setVisibility(View.GONE);
//                return false;
//            }
//        });
//    }
//
//    private void startNoTouch() {
//        handler.removeCallbacks(timeRun);
//        if (mDialogLl.getVisibility() == View.GONE) {
//            return;
//        }
//        handler.postDelayed(timeRun, 4000);
//    }
//
//    private void takePhotoPreview(byte[] data, Camera camera) {
//        if (!isGetPreview) {
//            return;
//        }
//        isGetPreview = false;
//        final byte[] fdata = data;
//        final Camera fcamera = camera;
//        final int fmode = mode;
//        final String fcardNo = cardNo;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Camera.Size size = fcamera.getParameters().getPreviewSize();
//                try {
//                    YuvImage image = new YuvImage(fdata, ImageFormat.NV21, size.width, size.height, null);
//                    if (image != null) {
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
//
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//
//                        //**********************
//                        //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
//                        Matrix matrix = new Matrix();
////                        matrix.preRotate(90);
//                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                        //**********************************
//                        if (bitmap != null) {
//                            LogUtils.d("拍照成功   开始压缩");
//                        } else {
//                            LogUtils.d("拍照失败   开始压缩");
//                        }
//                        //创建并保存临时原图片文件
//                        final File pictureFile = new File(AppConfig.getInstance().APP_PATH_ROOT, System.currentTimeMillis() + ".jpg");
//                        FileOutputStream fos = new FileOutputStream(pictureFile);
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                        if (bitmap.isRecycled()) {
//                            bitmap.recycle();
//                        }
//                        new Compressor(MainActivity1.this)
//                                .setDestinationDirectoryPath(AppConfig.getInstance().APP_PATH_ROOT + "/upload")
//                                .compressToFileAsFlowable(pictureFile)
//                                .subscribeOn(Schedulers.io())
////                        .observeOn(AndroidSchedulers.mainThread())
//                                .observeOn(Schedulers.newThread())
//                                .subscribe(new Consumer<File>() {
//                                    @Override
//                                    public void accept(File file) {
//                                        FileUtils.deleteFile(pictureFile);//删除原图
//                                        LogUtils.d("delete 原图, 压缩后file = " + file.getAbsolutePath());
//                                        //上传开门拍照信息
//                                        iPresenter.uploadOpenInfo(fmode, fcardNo, file);
//                                    }
//                                }, new Consumer<Throwable>() {
//                                    @Override
//                                    public void accept(Throwable throwable) {
////                                throwable.printStackTrace();
//                                        LogUtils.d("保存照片失败 throwable " + throwable.getMessage());
//                                    }
//                                });
//                        fos.close();
//                        stream.close();
//                    }
//                } catch (Exception ex) {
//                    LogUtils.e("Sys", "Error:" + ex.getMessage());
//                }
//            }
//        }).start();
//    }
//
//    private void initAudioRaw() {
//        spPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
//        sound = spPool.load(this, R.raw.open_door, 1);
//        spPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                LogUtils.d("soundPool load complete");
//            }
//        });
//    }
//
//    /**
//     * 检测用户输入，#号开头5位，为密码开门，密码微信生成临时密码，到后台验证
//     * 非#开头，验证是否为房号，则拨打对应房号绑定的手机号
//     *
//     * @param input
//     * @return
//     */
//    private int formartInput(String input) {
//        if (StringUtil.isNullOrEmpty(input)) {
//            return -1;
//        }
//        if (input.indexOf("#") > 0) {
//            return INPUT_ERROR;
//        }
//        if (input.startsWith("#")) {
//            if (input.length() == 7) {
//                return INPUT_OPEN;
//            } else {
//                if (input.length() > 7) {
//                    return INPUT_ERROR;
//                }
//                return -1;
//            }
//        } else {
//            if (input.length() > 4) {
//                return INPUT_ERROR;
//            }
//            if (input.length() == 4) {
//                return INPUT_CALL;
//            }
//            return -1;
//        }
//    }
//
//    CallStateListener callStateListener = new CallStateListener() {
//
//        @Override
//        public void onCalling() {
//
//        }
//
//        @Override
//        public void onRinging() {
//
//        }
//
//        @Override
//        public void onAnswer() {
//
//        }
//
//        @Override
//        public void onCallFailed(int i) {
//
//        }
//
//        @Override
//        public void onDisconnected() {
//
//        }
//
//        @Override
//        public void onMonitorQuality(int i) {
//
//        }
//    };
//
//    private void showDialog(boolean isAvi, @StringRes int resid) {
////        ToastShow.show(instence, resid);
//        mDialogLl.setVisibility(View.VISIBLE);
//        if (isAvi) {
//            mAvi.setVisibility(View.VISIBLE);
//        } else {
//            mAvi.setVisibility(View.GONE);
//        }
//        mContent.setText(resid);
//    }
//
//    private void showDialog(boolean isAvi, String res) {
//        mDialogLl.setVisibility(View.VISIBLE);
//        if (isAvi) {
//            mAvi.setVisibility(View.VISIBLE);
//        } else {
//            mAvi.setVisibility(View.GONE);
//        }
//        mContent.setText(res);
//    }
//
//    private void hideDialog() {
////        ToastShow.show(instence, "隐藏对话框");
//        handler.removeCallbacks(timeRun);
//        inputStr.delete(0, inputStr.length());
//        mDialogLl.setVisibility(View.GONE);
//        mContent.setText("");
//    }
//
//    private void releaseCamera() {
//        if (mCamera != null) {
//            mCamera.setPreviewCallback(null);
//            mCamera.release();
//            mCamera = null;
//            FrameLayout preview = findViewById(R.id.camera);
//            preview.removeView(mCameraPreviewSurface);
//        }
//    }
//
//    public static Camera getCameraInstance() {
//        Camera c = null;
//        try {
//            c = Camera.open();
//        } catch (Exception e) {
//        }
//        return c;
//    }
//}
