package com.zzhou.entrance.guard.module;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csipsimple.api.SipCallSession;
import com.csipsimple.api.SipProfile;
import com.csipsimple.pjsip.CallStateCallBack;
import com.csipsimple.pjsip.PjSipService2;
import com.csipsimple.service.SipService;
import com.main.pjsip_voice.Session;
import com.wang.avi.AVLoadingIndicatorView;
import com.zzhou.entrance.guard.AppConfig;
import com.zzhou.entrance.guard.Constants;
import com.zzhou.entrance.guard.MyApplication;
import com.zzhou.entrance.guard.R;
import com.zzhou.entrance.guard.acs.CameraPreview;
import com.zzhou.entrance.guard.acs.Gpio;
import com.zzhou.entrance.guard.bean.AccountData;
import com.zzhou.entrance.guard.bean.Ads;
import com.zzhou.entrance.guard.bean.HouseData;
import com.zzhou.entrance.guard.bean.ImeiNo;
import com.zzhou.entrance.guard.module.mvp.IMainContract;
import com.zzhou.entrance.guard.module.mvp.MainPresenter;
import com.zzhou.entrance.guard.netty.activity.NettyActivity;
import com.zzhou.entrance.guard.netty.bean.MessageInfo;
import com.zzhou.entrance.guard.netty.bean.RequestCmd;
import com.zzhou.entrance.guard.sendNotify.SendNotify;
import com.zzhou.entrance.guard.source.Ws;
import com.zzhou.entrance.guard.util.FileUtils;
import com.zzhou.entrance.guard.util.LogUtils;
import com.zzhou.entrance.guard.util.SystemTool;
import com.zzhou.entrance.guard.util.ToastShow;
import com.zzhou.entrance.guard.widget.CustomerVideoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import id.zelory.compressor.Compressor;
import io.netty.util.internal.StringUtil;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 屏保页面，无操作时显示界面，后台处理相关网络请求，IM消息
 * Created by The Moss on 2018/8/31.
 * http://js.emic.com.cn:1046/
 * admin_02566695505  admin02566695505
 */
public class MainActivity extends NettyActivity implements IMainContract.IView, SensorEventListener, CallStateCallBack {
    ImageView mAdsImage;
    CustomerVideoView mVideoView;
    LinearLayout mDialogLl;
    TextView mTitle;
    TextView mContent;
    AVLoadingIndicatorView mAvi;

//    private CameraPreview mySurfaceView;

//    static MainActivity instence;

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private Tag mTag;
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private Sensor mTemperatureSensor;

    //control and detect IO
    private static int ACS_IO_OFF = 0;
    private static int ACS_IO_ON = 1;
    private static int ACS_IO_MODE_IN = 0;
    private static int ACS_IO_MODE_OUT = 1;

    private static int ACS_IO_OUT_KEYPAD_LED = 89;
    private static int ACS_IO_OUT_CAMERA_LED = 65;
    private static int ACS_IO_OUT_DOOR1 = 76;
    private static int ACS_IO_OUT_DOOR2 = 77;
    private static int ACS_IO_OUT_DOOR3 = 78;
    private static int ACS_IO_OUT_DOOR4 = 79;

    private static int MSG_MAGNETIC1_STATE = 0;
    private static int MSG_MAGNETIC2_STATE = 0;

    int DOOR1_STATE = ACS_IO_ON;
    int DOOR2_STATE = ACS_IO_ON;

    int countDoor1;//1门磁1秒通知一次状态，记录次数，当超过4次  锁开后，门磁还未检测到门开 则自动锁门
    int countDoor2;//2门磁1秒通知一次状态，记录次数，当超过4次  锁开后，门磁还未检测到门开 则自动锁门
    static int MAX_COUNT_DOOR = 10;
    static boolean WARN_FIR = false;//火警告警标志
    static boolean LIGHT_POINT = false;

    IMainContract.IPresenter iPresenter;
    StringBuffer inputStr;

    final static int INPUT_OPEN = 0;
    final static int INPUT_CALL = 1;
    final static int INPUT_MOBILE = 3;
    final static int INPUT_ERROR = 2;

    static final int MODE_CARD = Constants.MODE_CARD;//刷卡
    static final int MODE_WXPASS = Constants.MODE_WXPASS;//微信
    static final int MODE_REMOTE = Constants.MODE_REMOTE;//远程
    static final int MODE_CALL = Constants.MODE_CALL;//呼叫
    static final int MODE_SENDWX = Constants.MODE_SENDWX;//呼叫时抓拍照片，推送到微信公众号
    static final int MODE_CALLPHONE = Constants.MODE_CALLPHONE;//直接呼叫手机号
    int mode = MODE_CARD;

    List<Bitmap> imslist = new ArrayList();
    int time;
    String cardNo = "";
    private boolean isOnLongPass = false;//长按#
    private boolean isShowMac = false;//是否正在显示mac地址
    boolean isOpenDooring = false;//是否正在开门

    private SoundPool spPool;
    int sound;

    //toCallId[]存储返回的通话的标识，这个标记用于挂断电话
    private static int toCallId[] = {-1};
    //状态
    private static String state;
    //对方sip的信息
    private static String toCall;
    private static int seconds;
    //通话id，用于操作接听或者拒接来电
    private static int callIds;
    private static ArrayList<String> PHONES = new ArrayList<String>();
    private static int which;
    private static PjSipService2 sipService;
    private boolean created = false;
    AudioManager audioManager;

    @SuppressLint("HandlerLeak")
    Handler imageHandler = new Handler();
    Runnable imageRun = new Runnable() {
        @Override
        public void run() {
            if (imslist == null || imslist.size() < 1) {
                return;
            }
            time++;
            if (time == imslist.size()) {
                time = 0;
            }
            Bitmap bitmap = imslist.get(time % imslist.size());
            mAdsImage.setImageBitmap(bitmap);
            if (imslist.size() > 1) {//超过1张图片，则延迟6秒后切换下一张
                imageHandler.postDelayed(imageRun, 6000);
            }
        }
    };
    private Handler longPassHanlder = new Handler();

    Runnable longRun = new Runnable() {
        @Override
        public void run() {
            if (isOnLongPass) {
                isShowMac = true;
                inputStr.delete(0, inputStr.length());
                showDialog(false, MyApplication.getInstance().getDeviceNo(), false);
            }
        }
    };
    //无操作倒计时
    Handler handler = new Handler();
    private Runnable timeRun = new Runnable() {
        @Override
        public void run() {
            LogUtils.d(">>>>>>>>> hide dialog ");
            hideDialog();
        }
    };
    //camera preview
    private Camera mCamera;
    private CameraPreview mCameraPreviewSurface;
    private boolean isGetPreview = false;
    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            takePhotoPreview(data, camera);
        }
    };
    private String callNo = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mySurfaceView = findViewById(R.id.activity_camera_surfaceview);
        mAdsImage = (ImageView) findViewById(R.id.activity_screen_image);
        mVideoView = (CustomerVideoView) findViewById(R.id.activity_screen_video);
        mDialogLl = (LinearLayout) findViewById(R.id.dialog_Ll);
        mContent = (TextView) findViewById(R.id.dialog_content);
        mTitle = (TextView) findViewById(R.id.dialog_title);
        mAvi = (AVLoadingIndicatorView) findViewById(R.id.dialog_avi);
        mTitle.setText(getResources().getString(R.string.app_splash, SystemTool.getAppVersionName(this)));
        iPresenter = new MainPresenter(this);
        inputStr = new StringBuffer();

        operateSipService(true);
        sipService.setCallBack(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        initAudioRaw();//加载音频资源

//        mAdsImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                iPresenter.getAdsInfo();
////                readCardlistener("wx0001");
////                cardNo = "04BB1D22EF4A80";
////                showDialog(true, R.string.on_calling_tips, false);
////                iPresenter.validateCard(cardNo);
//                iPresenter.callAccount("101");
//            }
//        });
        (findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PHONES.clear();
                handUp();
            }
        });
        (findViewById(R.id.call_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iPresenter.callAccount("101");
            }
        });
        showDialog(true, R.string.app_install_ing, true);
        iPresenter.getAccounts();
        iPresenter.getHouses();
        iPresenter.getLocalAdsInfo();
        iPresenter.imeiAccount();
        initNFC();
        initIO();
        initSensors();
    }

    protected void make_SharedPre(String houseNum){

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null && mPendingIntent != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
        if (mLightSensor != null) {
            mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mTemperatureSensor != null) {
            mSensorManager.registerListener(this, mTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        isOnLongPass = false;
        imageHandler.removeCallbacks(imageRun);
        if (imslist != null && imslist.size() > 0) {
            imageHandler.postDelayed(imageRun, 4000);
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mCamera == null) {
                    synchronized (MainActivity.this) {
                        if (mCamera == null) {
                            mCamera = getCameraInstance();
                            mCameraPreviewSurface = new CameraPreview(MainActivity.this, mCamera, previewCb);
                            FrameLayout preview = (FrameLayout) findViewById(R.id.camera);
                            preview.addView(mCameraPreviewSurface);
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
        if (mLightSensor != null) {
            mSensorManager.unregisterListener(this, mLightSensor);
        }
        if (mTemperatureSensor != null) {
            mSensorManager.unregisterListener(this, mTemperatureSensor);
        }
        imageHandler.removeCallbacks(imageRun);
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
        if (spPool != null) {
            spPool.stop(sound);
            spPool.release();
        }
//        ToastShow.show(this, "执行 onDestroy");
        super.onDestroy();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mTag == null) {
            return;
        }
        String[] techList = mTag.getTechList();
        LogUtils.d(TAG, "======tag tech list======");
        for (int i = 0; i < techList.length; i++) {
            LogUtils.d(TAG, techList[i]);
            if (techList[i].equals(MifareClassic.class.getName())) {
                readMifareClassic(mTag);
                break;
            } else if (techList[i].equals(Ndef.class.getName())) {
                readNdefTag(mTag);
                break;
            } else if (techList[i].equals(IsoDep.class.getName())) {
                readIsoDep(mTag);
                break;
            } else if (techList[i].equals(NfcA.class.getName())) {
                readNfcA(mTag);
                break;
            } else if (techList[i].equals(NfcB.class.getName())) {
                readNfcB(mTag);
                break;
            } else if (techList[i].equals(NfcV.class.getName())) {
                readNfcV(mTag);
                break;
            } else {
                LogUtils.d(TAG, "TODO: need parser!");
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isOpenDooring) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
//                ToastShow.show(this, "onKeyDown KEYCODE_1");
                inputStr.append("\t1");
                break;
            case KeyEvent.KEYCODE_2:
//                ToastShow.show(this, "onKeyDown KEYCODE_2");
                inputStr.append("\t2");
                break;
            case KeyEvent.KEYCODE_3:
//                ToastShow.show(this, "onKeyDown KEYCODE_3");
                inputStr.append("\t3");
                break;
            case KeyEvent.KEYCODE_4:
//                ToastShow.show(this, "onKeyDown KEYCODE_4");
                inputStr.append("\t4");
                break;
            case KeyEvent.KEYCODE_5:
//                ToastShow.show(this, "onKeyDown KEYCODE_5");
                inputStr.append("\t5");
                break;
            case KeyEvent.KEYCODE_6:
//                ToastShow.show(this, "onKeyDown KEYCODE_6");
                inputStr.append("\t6");
                break;
            case KeyEvent.KEYCODE_7:
//                ToastShow.show(this, "onKeyDown KEYCODE_7");
                inputStr.append("\t7");
                break;
            case KeyEvent.KEYCODE_8:
//                ToastShow.show(this, "onKeyDown KEYCODE_8");
                inputStr.append("\t8");
                break;
            case KeyEvent.KEYCODE_9:
//                ToastShow.show(this, "onKeyDown KEYCODE_9");
                inputStr.append("\t9");
                break;
            case KeyEvent.KEYCODE_0:
//                ToastShow.show(this, "onKeyDown KEYCODE_0");
                inputStr.append("\t0");
                break;
            case KeyEvent.KEYCODE_STAR://'*' key
//                ToastShow.show(this, "onKeyDown KEYCODE_STAR");
                break;
            case KeyEvent.KEYCODE_POUND://'#' key
//                LogUtils.d(TAG, "onKeyDown KEYCODE_POUND");
//                if (inputStr.length() > 0) {
//                    isOnLongPass = false;
//                    break;
//                }
                if (!isOnLongPass) {
                    inputStr.append("\t#");
                    isOnLongPass = true;
                    longPassHanlder.postDelayed(longRun, 3000);
                }
                break;

            //detect io1-4, F1-F4
            case KeyEvent.KEYCODE_F1:
                MSG_MAGNETIC1_STATE = ACS_IO_OFF;
                DOOR1_STATE = getIO(ACS_IO_OUT_DOOR1);
                if (DOOR1_STATE == ACS_IO_ON) {
                    countDoor1++;
                    if (countDoor1 >= MAX_COUNT_DOOR) {
                        unlock(false);
                        countDoor1 = 0;
                    }
                }
                break;//门磁关
            case KeyEvent.KEYCODE_F2:
                MSG_MAGNETIC2_STATE = ACS_IO_OFF;
                DOOR2_STATE = getIO(ACS_IO_OUT_DOOR2);
                if (DOOR2_STATE == ACS_IO_ON) {
                    countDoor2++;
                    if (countDoor2 >= MAX_COUNT_DOOR) {
                        unlockSecond(false);
                        countDoor2 = 0;
                    }
                }
                break;
            case KeyEvent.KEYCODE_F3:
                return true;
            case KeyEvent.KEYCODE_F4:
                return true;
            default:
                break;
        }
        if (keyCode == KeyEvent.KEYCODE_F2 || keyCode == KeyEvent.KEYCODE_F1) {
            if (isOnCountTime) {
                if (MSG_MAGNETIC1_STATE == ACS_IO_OFF && MSG_MAGNETIC2_STATE == ACS_IO_OFF) {
                    isOnCountTime = false;
                    startMagneticCountDwon();//关闭长时间未关门告警延迟
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        LogUtils.d(TAG, "onKeyUp keycode " + keyCode);
        if (isOpenDooring) {
            ToastShow.show(this, "正在开门，请稍后....");
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
//                ToastShow.show(this, "onKeyUp KEYCODE_1");
                break;
            case KeyEvent.KEYCODE_2:
//                ToastShow.show(this, "onKeyUp KEYCODE_2");
                break;
            case KeyEvent.KEYCODE_3:
//                ToastShow.show(this, "onKeyUp KEYCODE_3");
                break;
            case KeyEvent.KEYCODE_4:
//                ToastShow.show(this, "onKeyUp KEYCODE_4");
                break;
            case KeyEvent.KEYCODE_5:
//                ToastShow.show(this, "onKeyUp KEYCODE_5");
                break;
            case KeyEvent.KEYCODE_6:
//                ToastShow.show(this, "onKeyUp KEYCODE_6");
                break;
            case KeyEvent.KEYCODE_7:
//                ToastShow.show(this, "onKeyUp KEYCODE_7");
                break;
            case KeyEvent.KEYCODE_8:
//                ToastShow.show(this, "onKeyUp KEYCODE_8");
                break;
            case KeyEvent.KEYCODE_9:
//                ToastShow.show(this, "onKeyUp KEYCODE_9");
                break;
            case KeyEvent.KEYCODE_0:
//                ToastShow.show(this, "onKeyUp KEYCODE_0");
                break;
            case KeyEvent.KEYCODE_STAR://'*' key
//                ToastShow.show(this, "onKeyUp KEYCODE_STAR");
                LogUtils.d(TAG, "onKeyUp KEYCODE_STAR");
                PHONES.clear();
                handUp();
                hideDialog();
                return true;
            case KeyEvent.KEYCODE_POUND://'#' key
//                LogUtils.d(TAG, "onKeyUp KEYCODE_POUND");
                isOnLongPass = false;
                longPassHanlder.removeCallbacks(longRun);
                if (isShowMac) {
                    isShowMac = false;
                    return true;
                }
                break;

            //detect io1-4, F1-F4
            case KeyEvent.KEYCODE_F1:
                hideDialog();
                countDoor1 = 0;
                MSG_MAGNETIC1_STATE = ACS_IO_ON;
                DOOR1_STATE = getIO(ACS_IO_OUT_DOOR1);
                if (DOOR1_STATE == ACS_IO_ON) {//门磁开，锁开，则关门
                    unlock(false);
                }
                break;
            case KeyEvent.KEYCODE_F2:
                hideDialog();
                countDoor2 = 0;
                MSG_MAGNETIC2_STATE = ACS_IO_ON;
                DOOR2_STATE = getIO(ACS_IO_OUT_DOOR2);
                if (DOOR2_STATE == ACS_IO_ON) {//门磁开，锁开，则关门
                    unlockSecond(false);
                }
                break;
            case KeyEvent.KEYCODE_F3:
                return true;
            case KeyEvent.KEYCODE_F4:
                return true;
            default:
                break;
        }
        if (keyCode == KeyEvent.KEYCODE_F2 || keyCode == KeyEvent.KEYCODE_F1) {
            if (!isOnCountTime) {
                if (MSG_MAGNETIC1_STATE == ACS_IO_ON || MSG_MAGNETIC2_STATE == ACS_IO_ON) {
                    //长时间未关门只告警一次，关门后重置
                    isOnCountTime = true;
                    closeMagneticCountDwon();
                }
            }
            return true;//门磁检测，并非输入，跳出方法
        }

        showDialog(false, inputStr.toString(), true);
        int format = formartInput(inputStr.toString().replaceAll("\t", ""));
//        Toast.makeText(this, "format = " + format, Toast.LENGTH_SHORT).show();
        switch (format) {
            case INPUT_OPEN:
                handler.removeCallbacks(timeRun);//正在验证账号去掉倒计时
//                openStips("正在开门，请稍后！");
                if (isOpenDooring) {
                    showDialog(false, R.string.on_reopening_door_tips, false);
                    return true;
                }
                showDialog(true, R.string.on_opening_door_tips, false);
                isOpenDooring = true;
                // TODO: 2018/9/20  后台验证密码是否正确
                iPresenter.validatePass(inputStr.toString().replaceAll("#", "")
                        .replaceAll("\t", ""));
                return true;
            case INPUT_CALL:
                //拨打电话----键盘输入的房号
                mode = MODE_SENDWX;
                cardNo = inputStr.toString().replaceAll("#", "")
                        .replaceAll("\t", "");
                isGetPreview = true;
                handler.removeCallbacks(timeRun);
                showDialog(true, R.string.on_calling_tips, false);//正在呼叫，请稍后...
                // TODO: 2018/9/20 呼叫中心接口
                iPresenter.callAccount(cardNo);
                inputStr.delete(0, inputStr.length());
                return true;
            case INPUT_MOBILE:
                //拨打手机号---键盘输入的手机号
                LogUtils.d(TAG, "验证按键操作为：输入手机号--------即将开始呼叫手机号流程");
                mode = MODE_SENDWX;//设置抓拍图片参数
                String mobileNo = inputStr.toString().replaceAll("#", "")
                        .replaceAll("\t", "");
                isGetPreview = true;
                handler.removeCallbacks(timeRun);
                showDialog(true, R.string.on_mobile_calling_tips, false);//正在呼叫手机号，请稍后...
                // TODO: 2018/9/20 呼叫中心接口
                iPresenter.callMobileAccount(mobileNo);
                inputStr.delete(0, inputStr.length());
                return true;
            case INPUT_ERROR:
                hideDialog();
                return true;
            default:
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 处理服务器推送的消息
     * 1、获取服务端推送消息中的houseNo
     * @param message
     * @return
     */
    protected String formatmess(MessageInfo message){
        String mess_content = message.getContent();
        LogUtils.d(TAG,"notifyData--:"+mess_content);
        if(!mess_content.isEmpty()){
            String[] content = mess_content.split(":");
            return content[1];
        }
        return null;
    }

    //接收服务端推送的消息
    @Override
    protected void notifyData(MessageInfo message) {
        int cmd = message.getCmd();
        String houseNo = "0000";
        switch (cmd) {
            case RequestCmd.OPEN_DOOR:
                // TODO: 2018/9/14 远程开门
//                Toast.makeText(this, "******************远程开门*******************", Toast.LENGTH_SHORT).show();
                LogUtils.d("******************远程开门 双开*******************");
                isOpenDooring = true;
                mode = MODE_REMOTE;
                cardNo = message.getContent();
                houseNo = formatmess(message);
                openDoor(2,houseNo,MODE_REMOTE);
                break;
            case RequestCmd.OPEN_DOOR1:
                // TODO: 2018/9/14 远程开门
//                Toast.makeText(this, "******************远程开门*******************", Toast.LENGTH_SHORT).show();
                LogUtils.d("******************远程开门 门1*******************");
                isOpenDooring = true;
                mode = MODE_REMOTE;
                cardNo = message.getContent();
                houseNo = formatmess(message);
                openDoor(0,houseNo,MODE_REMOTE);
                break;
            case RequestCmd.OPEN_DOOR2:
                // TODO: 2018/9/14 远程开门
//                Toast.makeText(this, "******************远程开门*******************", Toast.LENGTH_SHORT).show();
                LogUtils.d("******************远程开门 门2*******************");
                isOpenDooring = true;
                mode = MODE_REMOTE;
                cardNo = message.getContent();
                houseNo = formatmess(message);
                openDoor(1,houseNo,MODE_REMOTE);
                break;
            case RequestCmd.UPDATE_ADS:
                // TODO: 2018/9/14 更新广告
//                Toast.makeText(this, "******************更新广告*******************", Toast.LENGTH_SHORT).show();
                LogUtils.d("******************更新广告*******************");
                iPresenter.getAdsInfo();
                break;
            case RequestCmd.UPDATE_ACCOUNTS:
                // TODO: 2018/9/14 更新用户数据
//                Toast.makeText(this, "******************更新用户数据*******************", Toast.LENGTH_SHORT).show();
                LogUtils.d("******************更新用户数据*******************");
                iPresenter.getAccounts();
                iPresenter.getHouses();
                break;
            case RequestCmd.UPDATE_VERSION:
                // TODO: 2018/9/14 更新版本
//                Toast.makeText(this, "******************更新版本*******************", Toast.LENGTH_SHORT).show();
                LogUtils.d("******************更新版本*******************");
                iPresenter.updateVersion();
                break;
        }
    }

    @Override
    public void channelAdsResult(boolean isSuccess, List<Ads> adslsit) {
        if (isSuccess) {
            Ads ads = adslsit.get(0);//视频只有一条，图片可以多条，这里简单处理下，判断第一个数据是视频还是图片
            LogUtils.d("ads type = " + ads.getType() + " url = " + ads.getUrl());
            if (ads.getType() == 0) {
                if (mVideoView.isPlaying()) {
                    mVideoView.stopPlayback();
                }
                imslist.clear();
                for (int i = 0; i < adslsit.size(); i++) {
                    if (FileUtils.fileExists(adslsit.get(i).getUrl())) {
                        Bitmap bitmap = BitmapFactory.decodeFile(adslsit.get(i).getUrl());
                        imslist.add(bitmap);
                    }
                }
                mVideoView.setVisibility(View.GONE);
                mAdsImage.setVisibility(View.VISIBLE);
                imageHandler.post(imageRun);
            } else {
//                String url = "http://jzvd.nathen.cn/63f3f73712544394be981d9e4f56b612/69c5767bb9e54156b5b60a1b6edeb3b5-5287d2089db37e62345123a1be272f8b.mp4";
                imageHandler.removeCallbacks(imageRun);
                String url = "";
                if (!StringUtil.isNullOrEmpty(ads.getUrl())) {
                    url = ads.getUrl();
                }
                if (StringUtil.isNullOrEmpty(url) || !url.endsWith(".mp4")) {
                    return;
                }
                if (FileUtils.fileExists(url)) {
                    mVideoView.setVisibility(View.VISIBLE);
                    mAdsImage.setVisibility(View.GONE);
                    playVideo(url);
                } else {
                    LogUtils.e("video path not exist, may be by deleted");
                }
            }
        }
    }

    @Override
    public void validateAccountResult(boolean isSuccess, AccountData account) {
        if (isSuccess) {
            mode = MODE_CARD;
            if (account != null) {
                cardNo = account.getCard();
                openDoor(account.getJurisdiction(),account.getNo(),MODE_CARD);
            } else {
                String tips = "No: " + cardNo;
                showDialog(false, tips, true);
                isOpenDooring = false;
            }
        } else {
            String tips = "No: " + cardNo;
            showDialog(false, tips, true);
            isOpenDooring = false;
        }
//        openStips("");
    }

    @Override
    public void imeiAccountResult(boolean isSuccess, ImeiNo imei, String msg) {
        if (isSuccess && imei != null) {
            registEMICall(imei.getBoardNumber(), imei.getExtensionNumber(), imei.getPass());
        }
    }

    /**
     * 呼叫房间号结果
     * 实现接口 IMainContract.IView-callAccountResult
     * @param isSuccess
     * @param msg
     * @param which
     */
    @Override
    public void callAccountResult(boolean isSuccess, String msg, int which) {
        LogUtils.d("call result success = " + isSuccess + " msg = " + msg + "  which = " + which);
        if (!isSuccess) {
            showDialog(false, R.string.call_fail, true);
        } else {
            if (StringUtil.isNullOrEmpty(msg)) {
                showDialog(false, R.string.call_fail_number, true);
                return;
            }
            this.which = which;
            try {
                List list = Arrays.asList(msg.split(","));
                PHONES = new ArrayList<String>(list);
                if (PHONES != null && PHONES.size() > 0) {
                    callNo = PHONES.remove(0);
                    makeCall(callNo);
                }
            } catch (Exception e) {
                LogUtils.d(e.getMessage());
            }
        }
    }

    /**
     * 呼叫手机号结果
     * 实现接口 IMainContract.IView-callMobileAccountResult
     * @param isSuccess
     * @param msg
     * @param which
     */
    @Override
    public void callMobileAccountResult(boolean isSuccess, String msg, int which) {
        LogUtils.d("呼叫流程step3_model_callMobileAccountResult：call result success = " + isSuccess + " msg = " + msg + "  which = " + which);
        if (!isSuccess) {
            showDialog(false, R.string.call_fail, true);
        } else {
            if (StringUtil.isNullOrEmpty(msg)) {
                showDialog(false, R.string.call_fail_number, true);
                return;
            }
            this.which = which;
            try {
                List list = Arrays.asList(msg.split(","));
                PHONES = new ArrayList<String>(list);
                if (PHONES != null && PHONES.size() > 0) {
                    callNo = PHONES.remove(0);
                    makeCall(callNo);
                }
            } catch (Exception e) {
                LogUtils.d(e.getMessage());
            }
        }
    }

    /**
     * 验证开门临时密码-并执行开门操作
     * @param isSuccess
     * @param msg
     */
    @Override
    public void validatePass(boolean isSuccess, String msg) {
        inputStr.delete(0, inputStr.length());
        if (isSuccess) {
            mode = MODE_WXPASS;
            cardNo = msg;
            openDoor(2,"临时密码",MODE_WXPASS);
        } else {
            showDialog(false, R.string.open_door_passfail, true);
            isOpenDooring = false;
        }
    }

    /***************************呼叫监听***********************************/
    /**
     * 在这里跳转到通话界面或者处理通话状态
     * 来电选择接听后进入这里
     * 呼出对方接听后进入
     */
    @Override
    public void callStateUpdate(int i, String s, String s1) {
        LogUtils.d("callStateUpdate i = " + i + " s = " + s + "  s1 = " + s1);
        if (Constants.CallState.CALLING.equals(s)) {
            showDialog(true, R.string.on_callinged, false);
            return;
        }
        if (Constants.CallState.COMFIRMED.equals(s)) {
            showDialog(true, R.string.call_on, false);
            PHONES.clear();
            return;
        }
        if (Constants.CallState.CONNECTING.equals(s)) {
            return;
        }
        if (Constants.CallState.INVALID.equals(s)) {
            showDialog(true, R.string.call_fail_number, true);
            return;
        }
        if (Constants.CallState.DISCONNECTED.equals(s)) {
//            handUp();
            if (PHONES != null && PHONES.size() > 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (PHONES != null && PHONES.size() > 0) {
                                handUp();
                                toCallId[0] = -1;
                                callNo = PHONES.remove(0);
                                makeCall(callNo);
                            }
                        } catch (Exception e) {
                            toCallId[0] = -1;
                            hideDialog();
                            LogUtils.e(e.getMessage());
                        }
                    }
                }, 1 * 1000);
                showDialog(true, R.string.call_finish, false);
            } else {
                toCallId[0] = -1;
                hideDialog();
            }
            return;
        }
    }

    //通话状态回调 incoming,calling,connecting,comfirmed,invalid,disconnected.
    @Override
    public void callStateUpdate(String s) {
        LogUtils.d("callStateUpdate  s = " + s);
    }

    //来电回调
    @Override
    public void inComingCall(int i, String s) {
        LogUtils.d("inComingCall i = " + i + " s = " + s);
    }

    @Override
    public void netStateUpdate(String s, int i) {
        LogUtils.d("netStateUpdate i = " + i + " s = " + s);
    }

    /**
     * 接收DTMF信号
     * 此操作：接通电话后，点击键盘上的'#'
     * @param s
     */
    @Override
    public void receiveDtmf(String s) {
        LogUtils.d("recive dtmf s = " + s);
        if ("#".equals(s)) {
            mode = MODE_CALL;
            String houseNum = cardNo;
            cardNo = cardNo + ":"+ callNo;
            PHONES.clear();
            openDoor(which,houseNum,MODE_CALL);
        }

    }
    /*******************************************************************/
    /**
     * 注册呼叫中心
     */
    private void registEMICall(final String serverIp, final String userId, final String password) {
        LogUtils.d("go regist EMICall serverIp = " + serverIp + "  userID = " + userId + " password = " + password);
        Session.setServerIP(serverIp);
        SipProfile file1 = Session.buildAccount(userId, userId, serverIp, password);
        Session.setSipProfile(file1);
        if (StringUtil.isNullOrEmpty(serverIp) || StringUtil.isNullOrEmpty(userId)) {
            LogUtils.d("register fail because serverIp or userId is empty");
            return;
        }
        try {
            if (sipService.setAccountRegistration(file1, 1, false)) {
                Session.getSipProfile().active = true;
                LogUtils.d(" register success");
            } else {
                LogUtils.d(" register fail reRegister");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        registEMICall(serverIp, userId, password);
                    }
                }, 6 * 1000);
            }

        } catch (SipService.SameThreadException e) {
            LogUtils.d(" register erro " + e.getMessage());
        }
    }

    //拨打电话
    private int makeCall(String callee) {
        if (!created) {
            return -1;
        }
        try {
            LogUtils.d("makeCall " + callee);
            return sipService.makeCall(callee, 1, null, toCallId);
        } catch (SipService.SameThreadException e) {
            e.printStackTrace();
            LogUtils.d("call error e " + e.getMessage());
        }
        return -1;
    }

    //挂断电话   （接听和挂断，拒接电话，都是调用同一个函数,,这里只用到挂断电话）
    private int handUp() {
        LogUtils.d("handUp " + toCallId[0]);
        if (created && toCallId[0] != -1) {
            try {
                return sipService.callHangup(toCallId[0], SipCallSession.StatusCode.DECLINE);
            } catch (SipService.SameThreadException e) {
                LogUtils.d("handUp error " + e.getMessage());
                return -1;
            }
        }
//        LogUtils.d("handUp fail created or toCallId is wrong");
        return -1;
    }

    //刷卡开门监听
    public void readCardlistener(String cardid) {
//        Toast.makeText(instence, "刷卡开门" + cardid, Toast.LENGTH_SHORT).show();
        if (isOpenDooring) {
            showDialog(false, R.string.on_reopening_door_tips, false);
            return;
        }
        // 验证卡号
        cardNo = cardid.replaceAll(" ", "");
        String tips = "No: " + cardNo;
        showDialog(true, tips, false);
        isOpenDooring = true;
        iPresenter.validateCard(cardNo);
    }

    public void overTimelistener() {
        iPresenter.uploadWarnInfo(0);
    }

    public void onTemperatureWarnListener() {
        iPresenter.uploadWarnInfo(1);
    }


    /**
     * @param which 开哪个门
     */
    private void openDoor(int which,String houseNum,int flag) {
        switch (which) {
            case 0:
                unlock(true);
                break;
            case 1:
                unlockSecond(true);
                break;
            case 2:
                unlock(true);
                unlockSecond(true);
                break;
            default:
                unlock(true);
                break;
        }
        showDialog(false, R.string.open_door_success, true);
        isOpenDooring = false;
//        if (SystemTool.checkNet(MainActivity.this)) {//无网络情况不拍照上传
        isGetPreview = true;
//        }
        new Thread() {
            @Override
            public void run() {
                spPool.play(sound, 1, 1, 1, 0, 1);
            }
        }.start();
        //调用SendNotify--开门后*弃用*
        //SendNotify.doorsendNotify(flag,houseNum,"开门结束");
    }

    private void playVideo(final String url) {
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setVideoPath(url);
        mVideoView.start();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
                mp.start();
                mp.setLooping(true);
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mVideoView.setVisibility(View.GONE);
                return false;
            }
        });
    }

    private synchronized void startNoTouch() {
        LogUtils.d(">>>>>>>>> start no touch ");
        handler.removeCallbacks(timeRun);
//        if (mDialogLl.getVisibility() == View.GONE) {
//            LogUtils.d(">>>>>>>>> dialog is gone ");
//            return;
//        }
        LogUtils.d(">>>>>>>>> postDelayed 6s ");
        handler.postDelayed(timeRun, 6000);
    }

    /**
     * 生成照片预览
     * @param data
     * @param camera
     */
    private void takePhotoPreview(byte[] data, Camera camera) {
        if (!isGetPreview) {
            return;
        }
        isGetPreview = false;
        final byte[] fdata = data;
        final Camera fcamera = camera;
        final int fmode = mode;
        final String fcardNo = cardNo;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Camera.Size size = fcamera.getParameters().getPreviewSize();
                try {
                    YuvImage image = new YuvImage(fdata, ImageFormat.NV21, size.width, size.height, null);
                    if (image != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);

                        Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

                        //**********************
                        //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                        Matrix matrix = new Matrix();
//                        matrix.preRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        //**********************************
                        if (bitmap != null) {
                            LogUtils.d("拍照成功   开始压缩");
                        } else {
                            LogUtils.d("拍照失败   开始压缩");
                        }
                        //创建并保存临时原图片文件
                        final File pictureFile = new File(AppConfig.getInstance().APP_PATH_ROOT, System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        if (bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        new Compressor(MainActivity.this)
                                .setDestinationDirectoryPath(AppConfig.getInstance().APP_PATH_ROOT + "/upload")
                                .compressToFileAsFlowable(pictureFile)
                                .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Consumer<File>() {
                                    @Override
                                    public void accept(File file) {
                                        FileUtils.deleteFile(pictureFile);//删除原图
                                        LogUtils.d("delete 原图, 压缩后file = " + file.getAbsolutePath());
                                        //上传开门拍照信息
                                        iPresenter.uploadOpenInfo(fmode, fcardNo, file);
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) {
//                                throwable.printStackTrace();
                                        LogUtils.d("保存照片失败 throwable " + throwable.getMessage());
                                    }
                                });
                        fos.close();
                        stream.close();
                    }
                } catch (Exception ex) {
                    LogUtils.e("Sys", "Error:" + ex.getMessage());
                }
            }
        }).start();
    }

    private void initAudioRaw() {
        spPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sound = spPool.load(this, R.raw.open_door, 1);
        spPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                LogUtils.d("soundPool load complete");
            }
        });
    }

    /**
     * 检测用户输入，#号开头5位，为密码开门，密码微信生成临时密码，到后台验证
     * 非#开头，验证是否为房号，则拨打对应房号绑定的手机号
     *
     * @param input
     * @return
     */
    private int formartInput(String input) {
        if (StringUtil.isNullOrEmpty(input)) {
            return -1;
        }
//        if (input.indexOf("#") > 0) {
//            return INPUT_ERROR;
//        }
        if (input.startsWith("#")) {//已'#'开头，临时密码开门
            if (input.length() == 7) {
                return INPUT_OPEN;
            } else {
                if (input.length() > 7) {
                    return INPUT_ERROR;
                }
                return -1;
            }
        } else { //非'#'开头，呼叫开门
            if(input.length()>6){ //手机号开门
                if (input.length() > 12) { //手机号+# 不能超过12位
                    return INPUT_ERROR;
                }
                if (input.length() < 6) { //手机号+# 不能小于6位
                    if (input.endsWith("#")) {
                        return INPUT_ERROR;
                    }
                    return -1;
                }
                if (input.endsWith("#")) {
                    return INPUT_MOBILE;
                }

            }else{ //房号开门
                if (input.length() > 6) {//房号+# 不能超过6位
                    return INPUT_ERROR;
                }
                if (input.length() < 4) {//房号+# 不能小于4位
                    if (input.endsWith("#")) {
                        return INPUT_ERROR;
                    }
                    return -1;
                }
                if (input.endsWith("#")) {
                    return INPUT_CALL;
                }
            }
            return -1;
        }
    }

    private synchronized void showDialog(final boolean isAvi, @StringRes final int resid, final boolean isStartNoTouch) {
//        ToastShow.show(instence, resid);
        LogUtils.d("showDialog isAvi = " + isAvi + "  resid = " + resid + "  isStartNoTouch = " + isStartNoTouch);
        if (isStartNoTouch) {
            startNoTouch();
        } else {
            handler.removeCallbacks(timeRun);
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mDialogLl.setVisibility(View.VISIBLE);
                if (isAvi) {
                    mAvi.show();
                } else {
                    mAvi.hide();
                }
                mContent.setText(resid);
            }
        });
    }

    private synchronized void showDialog(final boolean isAvi, final String res, final boolean isStartNoTouch) {
        LogUtils.d("showDialog isAvi = " + isAvi + "  res = " + res + "  isStartNoTouch = " + isStartNoTouch);
        if (isStartNoTouch) {
            startNoTouch();
        } else {
            handler.removeCallbacks(timeRun);
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mDialogLl.setVisibility(View.VISIBLE);
                if (isAvi) {
                    mAvi.show();
                } else {
                    mAvi.hide();
                }
                mContent.setText(res);
            }
        });
    }

    private synchronized void hideDialog() {
//        ToastShow.show(instence, "隐藏对话框");
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(timeRun);
                inputStr.delete(0, inputStr.length());
                mDialogLl.setVisibility(View.GONE);
                mContent.setText("");
            }
        });
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera);
            preview.removeView(mCameraPreviewSurface);
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private void operateSipService(boolean lauch) {
        if (sipService == null) {
            created = true;
            sipService = PjSipService2.instance(this);
            sipService.tryToLoadStack();
        }

        if (lauch) {
            try {
                sipService.launchPjSip();
                created = true;
                LogUtils.d("lunch sipservice success");
            } catch (SipService.SameThreadException e) {
                LogUtils.d("lunch sipservice error e " + e.getMessage());
            }
        } else {
            try {
                sipService.sipStop();
                created = false;
            } catch (SipService.SameThreadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

/******************************************************************************************/
    /**
     * 以下是NFC
     **/
    /**
     * 开关门 当门开着 则关门，当门关着 则开门
     *
     * @param open true开门
     */
    public void unlock(boolean open) {
        LogUtils.d("openDoor + open = " + open);
        int isOpen = getIO(ACS_IO_OUT_DOOR1);
        if (isOpen == ACS_IO_OFF) {
            if (open == true) {
                setIO(ACS_IO_OUT_DOOR1, ACS_IO_ON);
            }
        } else {
            if (!open) {
                setIO(ACS_IO_OUT_DOOR1, ACS_IO_OFF);
            }
        }
        isOpen = getIO(ACS_IO_OUT_DOOR1);
        if (isOpen == ACS_IO_ON) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        setIO(ACS_IO_OUT_DOOR1, ACS_IO_OFF);
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
        }
    }

    /**
     * 开关门
     *
     * @param open
     */
    public void unlockSecond(boolean open) {
        LogUtils.d("openSecondDoor + open = " + open);
        int isOpen = getIO(ACS_IO_OUT_DOOR2);
        if (isOpen == ACS_IO_OFF) {
            if (open == true) {
                setIO(ACS_IO_OUT_DOOR2, ACS_IO_ON);
            }
        } else {
            if (!open) {
                setIO(ACS_IO_OUT_DOOR2, ACS_IO_OFF);
            }
        }
        isOpen = getIO(ACS_IO_OUT_DOOR2);
        if (isOpen == ACS_IO_ON) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        setIO(ACS_IO_OUT_DOOR2, ACS_IO_OFF);
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
        }

    }

    @SuppressLint("NewApi")
    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, R.string.nfc_not_support, Toast.LENGTH_SHORT).show();
        } else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, R.string.nfc_disabled, Toast.LENGTH_SHORT).show();
            } else {
                // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
                // will fill in the intent with the details of the discovered tag before delivering to
                // this activity.
                mPendingIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

                // Setup an intent filter for all MIME based dispatches
                IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
                try {
                    ndef.addDataType("*/*");
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    throw new RuntimeException("fail", e);
                }
                mFilters = new IntentFilter[]{
                        ndef,
                };

                // Setup a tech list for all Ndef tags
                mTechLists = new String[][]{
                        new String[]{IsoDep.class.getName()},
                        new String[]{MifareClassic.class.getName()},
                        new String[]{MifareUltralight.class.getName()},
                        new String[]{Ndef.class.getName()},
                        new String[]{NfcA.class.getName()},
                        new String[]{NfcB.class.getName()},
                        new String[]{NfcBarcode.class.getName()},
                        new String[]{NfcF.class.getName()},
                        new String[]{NfcV.class.getName()},
                };
            }
        }

    }

    private void initIO() {
        //control IO
        Gpio.init(ACS_IO_OUT_KEYPAD_LED);
        Gpio.setMode(ACS_IO_OUT_KEYPAD_LED, ACS_IO_MODE_OUT);
        Gpio.init(ACS_IO_OUT_CAMERA_LED);
        Gpio.setMode(ACS_IO_OUT_CAMERA_LED, ACS_IO_MODE_OUT);

        Gpio.init(ACS_IO_OUT_DOOR1);
        Gpio.setMode(ACS_IO_OUT_DOOR1, ACS_IO_MODE_OUT);
        Gpio.init(ACS_IO_OUT_DOOR2);
        Gpio.setMode(ACS_IO_OUT_DOOR2, ACS_IO_MODE_OUT);
        Gpio.init(ACS_IO_OUT_DOOR3);
        Gpio.setMode(ACS_IO_OUT_DOOR3, ACS_IO_MODE_OUT);
        Gpio.init(ACS_IO_OUT_DOOR4);
        Gpio.setMode(ACS_IO_OUT_DOOR4, ACS_IO_MODE_OUT);
    }

    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mTemperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    }

    //refer: http://nfc-tools.org/index.php?title=ISO14443A
    private void readNfcA(Tag tag) {
        NfcA nfca = NfcA.get(tag);
        try {
            byte[] id = nfca.getTag().getId();
            byte[] atqa = nfca.getAtqa();
            short sak = nfca.getSak();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                int maxSize = nfca.getMaxTransceiveLength();
            }
//            mUidText.setText("UID:" + byteArrayToString(id, id.length));
            LogUtils.d(TAG, "id:" + bytesToHexString(id));
//            LogUtils.d(TAG, "ATQA:" + bytesToHexString(atqa));
//            LogUtils.d(TAG, "SAK:" + sak);
//            LogUtils.d(TAG, "maxSize:" + maxSize);
            //ISO/IEC 14443-3 Mifare Ultralight
//            if (atqa[0] == (byte) 0x44 && atqa[1] == (byte) 0x0) {
//                Toast.makeText(mContext, "Type: MIFARE Ultralight", Toast.LENGTH_SHORT).show();
//            } else {
//                //TODO:other format
//            }
            //TODO: read page??
            readCardlistener(byteArrayToString(id, id.length));
            nfca.connect();
        } catch (IOException e) {
            LogUtils.d(TAG, "readNfcA exception" + e.getMessage());
        } finally {
            try {
                nfca.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void readNfcB(Tag tag) {
        NfcB nfcb = NfcB.get(tag);
        try {
            byte[] id = nfcb.getTag().getId();
            byte[] appData = nfcb.getApplicationData();
            byte[] protocalData = nfcb.getProtocolInfo();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                int maxSize = nfcb.getMaxTransceiveLength();
            }
//            LogUtils.d(TAG, "id:" + bytesToHexString(id));
//            LogUtils.d(TAG, "appData:" + bytesToHexString(appData));
//            LogUtils.d(TAG, "protocalData:" + bytesToHexString(protocalData));
//            LogUtils.d(TAG, "maxSize:" + maxSize);
            //TODO: read page??
            nfcb.connect();
            byte[] query = {(byte) 0x05, (byte) 0x00, (byte) 0x00};
            byte[] select = {(byte) 0x1D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x01, (byte) 0x08};
            byte[] guid = {(byte) 0x00, (byte) 0x36, (byte) 0x00, (byte) 0x00, (byte) 0x08};
            byte[] respone = nfcb.transceive(guid);
            LogUtils.d(TAG, "guid return(" + respone.length + "):" + bytesToHexString(respone));
//            mUidText.setText("UID:" + byteArrayToString(respone, 8));
            readCardlistener(byteArrayToString(respone, 8));
        } catch (IOException e) {
            LogUtils.d(TAG, "readNfcB exception" + e.getMessage());
        } finally {
            try {
                nfcb.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void readNfcV(Tag tag) {// NFC-V (ISO 15693)
        NfcV nfcvTag = NfcV.get(tag);
        try {
            nfcvTag.connect();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Could not open a connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] cmd = new byte[]{
                    (byte) 0x00, // Flags
                    (byte) 0x23, // Command: Read multiple blocks
                    (byte) 0x00, // First block (offset)
                    (byte) 0x04  // Number of blocks
            };
            byte[] userdata = nfcvTag.transceive(cmd);

            userdata = Arrays.copyOfRange(userdata, 0, 32);
//            txtWrite.setText("DATA:" + Common.getHexString(userdata));
//            readCardlistener(bytesToHexString(userdata));
            readCardlistener(byteArrayToString(userdata, userdata.length));

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "An error occurred while reading!", Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, "readNfcV exception" + e.getMessage());
            return;
        } finally {
            try {
                nfcvTag.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void readIsoDep(Tag tag) {
//        IsoDep isoDep = IsoDep.get(tag);
//        try {
//            isoDep.connect();
//
//            byte[] SELECT = {
//                    (byte) 0x00, // CLA = 00 (first interindustry command set)
//                    (byte) 0xA4, // INS = A4 (SELECT)
//                    (byte) 0x04, // P1  = 04 (select file by DF name)
//                    (byte) 0x0C, // P2  = 0C (first or only file; no FCI)
//                    (byte) 0x06, // Lc  = 6  (data/AID has 6 bytes)
//                    (byte) 0x31, (byte) 0x35, (byte) 0x38, (byte) 0x34, (byte) 0x35, (byte) 0x46 // AID = 15845F
//            };
//
//            byte[] result = isoDep.transceive(SELECT);
//            LogUtils.i(TAG, "SELECT: " + byteArrayToString(result,result.length));
//            iPresenter.validateCard(byteArrayToString(result,result.length));
////            if (!(result[0] == (byte) 0x90 && result[1] == (byte) 0x00))
////                throw new IOException("could not select application");
////
////            byte[] GET_STRING = {
////                    (byte) 0x00, // CLA Class
////                    (byte) 0xB0, // INS Instruction
////                    (byte) 0x00, // P1  Parameter 1
////                    (byte) 0x00, // P2  Parameter 2
////                    (byte) 0x04  // LE  maximal number of bytes expected in result
////            };
////
////            result = isoDep.transceive(GET_STRING);
////            LogUtils.i(TAG, "GET_STRING: " + bytesToHexString(result));
//
//        } catch (IOException e) {
//            Toast.makeText(getApplicationContext(), "An error occurred while reading!", Toast.LENGTH_SHORT).show();
//            LogUtils.d(TAG, "readIsoDep exception" + e.getMessage());
//        } finally {
//            try {
//                isoDep.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }

    private void readMifareClassic(Tag tag) {
        MifareClassic mc = MifareClassic.get(tag);
        try {
            boolean auth = false;
            String metaInfo = "";
            mc.connect();
            int type = mc.getType();
            int sectorCount = mc.getSectorCount();
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "card type: " + typeS + " sectors: " + sectorCount + " blocks: " + mc.getBlockCount() + " size: " + mc.getSize() + "byte\n";
//            mUidText.setText(metaInfo);
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mc.authenticateSectorWithKeyA(j, MifareClassic.KEY_NFC_FORUM);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":auth ok\n";
                    bCount = mc.getBlockCountInSector(j);
                    bIndex = mc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":auth fail\n";
                }
            }
            LogUtils.d(TAG, metaInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void readNdefTag(Tag tag) {
        if (tag != null) {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                try {
                    ndef.connect();
                    NdefMessage msg = ndef.getNdefMessage();
                    if (msg != null) {
                        NdefRecord record = msg.getRecords()[0];
                        if (record != null) {
                            String readResult = new String(record.getPayload(), "UTF-8");
//                            LogUtils.d(TAG, "payload:" + record.toString());
                            LogUtils.d(TAG, "payload:" + readResult);
//                            mUidText.setText("payload:" + readResult);
                        }
                    } else {
                        LogUtils.d(TAG, "tag is empty!");
                    }
                } catch (IOException ie) {
                    ie.printStackTrace();
                } catch (FormatException fe) {
                    fe.printStackTrace();
                } finally {
                    try {
                        ndef.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                LogUtils.d(TAG, "Tag does not support Ndef");
//                Toast.makeText(mContext, "Tag does not support Ndef", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * @param buffer       byte array
     * @param bufferLength byte array length
     * @return
     * @brief convert byte array to string
     */
    public String byteArrayToString(byte[] buffer, int bufferLength) {
        if (buffer == null || bufferLength == 0) {
            return null;
        }

        String bufferString = "";
        String dbgString = "";

        for (int i = 0; i < bufferLength; i++) {
            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }
            if (i % 16 == 0) {
                if (dbgString != "") {
                    bufferString += dbgString;
                    dbgString = "";
                }
            }
            dbgString += hexChar.toUpperCase() + " ";
        }
        if (dbgString != "") {
            bufferString += dbgString;
        }
        return bufferString;
    }

    /**
     * @param index
     * @param state
     * @return
     * @brief set IO state, 1:high level,0:low level
     */
    private void setIO(int index, int state) {
        Gpio.set(index, state);
    }

    /**
     * @param index
     * @return
     * @brief get IO state, 1:high level,0:low level
     */
    private int getIO(int index) {
        return Gpio.get(index);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_LIGHT:
                if (event.values[0] > 900) {
                    if (!LIGHT_POINT) {
                        LIGHT_POINT = true;
                        setIO(ACS_IO_OUT_CAMERA_LED, ACS_IO_ON);
                        setIO(ACS_IO_OUT_KEYPAD_LED, ACS_IO_ON);
                    }
                } else if (event.values[0] < 600) {//防止临界点频繁切换
                    if (LIGHT_POINT) {
                        LIGHT_POINT = false;
                        setIO(ACS_IO_OUT_CAMERA_LED, ACS_IO_OFF);
                        setIO(ACS_IO_OUT_KEYPAD_LED, ACS_IO_OFF);
                    }
                }
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                if (event.values[0] > 900) {
                    if (!WARN_FIR) {
                        WARN_FIR = true;
                        onTemperatureWarnListener();
                    }
                } else if (event.values[0] < 600) {//防止临界点频繁切换
                    if (WARN_FIR) {
                        WARN_FIR = false;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //开门后倒计时 1分钟后门磁还没关在发告警
    Handler magneticHandler = new Handler();
    boolean isOnCountTime = false;
    private Runnable magneticTimeRun = new Runnable() {
        @Override
        public void run() {
            LogUtils.d("长时间没关门 MSG_MAGNETIC1_STATE = " + MSG_MAGNETIC1_STATE + "  MSG_MAGNETIC2_STATE = " + MSG_MAGNETIC2_STATE);
            if (MSG_MAGNETIC1_STATE == ACS_IO_ON
                    || MSG_MAGNETIC2_STATE == ACS_IO_ON) {
                if (isOnCountTime) {
                    overTimelistener();
                }
            }
        }
    };

    private void startMagneticCountDwon() {
        closeMagneticCountDwon();
        magneticHandler.postDelayed(magneticTimeRun, 60 * 1000);
    }

    private void closeMagneticCountDwon() {
        magneticHandler.removeCallbacks(magneticTimeRun);
    }
}
