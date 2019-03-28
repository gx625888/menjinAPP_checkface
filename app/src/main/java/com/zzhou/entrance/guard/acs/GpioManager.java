package com.zzhou.entrance.guard.acs;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.zzhou.entrance.guard.R;
import com.zzhou.entrance.guard.util.LogUtils;

import java.io.IOException;

/**
 * <desc>
 * Created by The Moss on 2018/9/19.
 */

public class GpioManager implements SensorEventListener, GpioMagneticListener {
    String TAG = this.getClass().getSimpleName();
    static Activity mContext;
    static GpioManager mManager;

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

    GpioListener mListener;

    int countDoor1;//1门磁1秒通知一次状态，记录次数，当超过4次  锁开后，门磁还未检测到门开 则自动锁门
    int countDoor2;//2门磁1秒通知一次状态，记录次数，当超过4次  锁开后，门磁还未检测到门开 则自动锁门
    static int MAX_COUNT_DOOR = 10;
    static boolean WARN_FIR = false;//火警告警标志
    static boolean LIGHT_POINT = false;

    private GpioManager(Activity context) {
        if (context instanceof GpioListener) {
            initialize((GpioListener) context);
        } else {
            new Throwable("activity no implements GpioListener");
        }
    }

    public interface GpioListener {
        //监听读卡
        void readCardlistener(String cardid);

        //监听门超时未关
        void overTimelistener();

        //温度过高告警，用于消防告警
        void onTemperatureWarnListener();
    }

    public static GpioManager getInstance(Activity context) {
        mContext = context;
        if (mManager == null)
            synchronized (GpioManager.class) {
                if (mManager == null)
                    mManager = new GpioManager(context);
            }
        return mManager;
    }

    private void initialize(GpioListener listener) {
        mListener = listener;
        initNFC();
        initIO();
        initSensors();
    }

    /**
     * 开关门 当门开着 则关门，当门关着 则开门
     *
     * @param open true开门
     */
    public void openDoor(boolean open) {
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
    public void openSecondDoor(boolean open) {
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

    /**
     * 摄像头灯光
     *
     * @param isOn
     */
    public void openCameraLight(boolean isOn) {
        setIO(ACS_IO_OUT_CAMERA_LED, isOn ? ACS_IO_ON : ACS_IO_OFF);
    }

    /**
     * 键盘灯
     *
     * @param isOn
     */
    public void openKeyBoardLight(boolean isOn) {
        setIO(ACS_IO_OUT_KEYPAD_LED, isOn ? ACS_IO_ON : ACS_IO_OFF);
    }

    public void onResume() {
        if (mNfcAdapter != null && mPendingIntent != null) {
            mNfcAdapter.enableForegroundDispatch(mContext, mPendingIntent, mFilters, mTechLists);
        }
        if (mLightSensor != null) {
            mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mTemperatureSensor != null) {
            mSensorManager.registerListener(this, mTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void onPause() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(mContext);
        }
        if (mLightSensor != null) {
            mSensorManager.unregisterListener(this, mLightSensor);
        }
        if (mTemperatureSensor != null) {
            mSensorManager.unregisterListener(this, mTemperatureSensor);
        }
    }

    public void onNewIntent(Intent intent) {
//        LogUtils.i(TAG, "Discovered tag with intent: " + intent);
        mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
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
            } else {
                LogUtils.d(TAG, "TODO: need parser!");
                break;
            }
        }
    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        if (mNfcAdapter == null) {
            Toast.makeText(mContext, R.string.nfc_not_support, Toast.LENGTH_SHORT).show();
        } else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(mContext, R.string.nfc_disabled, Toast.LENGTH_SHORT).show();
            } else {
                // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
                // will fill in the intent with the details of the discovered tag before delivering to
                // this activity.
                mPendingIntent = PendingIntent.getActivity(mContext, 0,
                        new Intent(mContext, mContext.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

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
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
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
            int maxSize = nfca.getMaxTransceiveLength();
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
            mListener.readCardlistener(byteArrayToString(id, id.length));
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
            int maxSize = nfcb.getMaxTransceiveLength();
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
            mListener.readCardlistener(byteArrayToString(respone, 8));
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

    private void readIsoDep(Tag tag) {
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
            Log.d(TAG, metaInfo);
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
                            Log.d(TAG, "payload:" + record.toString());
//                            mUidText.setText("payload:" + readResult);
                        }
                    } else {
                        Log.d(TAG, "tag is empty!");
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
//                setIO(ACS_IO_OUT_CAMERA_LED, event.values[0] > 1000 ? ACS_IO_ON : ACS_IO_OFF);
//                setIO(ACS_IO_OUT_KEYPAD_LED, event.values[0] > 1000 ? ACS_IO_ON : ACS_IO_OFF);
                if (event.values[0] > 1200) {
                    if (!LIGHT_POINT) {
                        LIGHT_POINT = true;
                        setIO(ACS_IO_OUT_CAMERA_LED,ACS_IO_ON);
                        setIO(ACS_IO_OUT_KEYPAD_LED,ACS_IO_ON);
                    }
                } else if (event.values[0] < 900) {//防止临界点频繁切换
                    if (LIGHT_POINT) {
                        LIGHT_POINT = false;
                        setIO(ACS_IO_OUT_CAMERA_LED,ACS_IO_OFF);
                        setIO(ACS_IO_OUT_KEYPAD_LED,ACS_IO_OFF);
                    }
                }
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                if (event.values[0] > 1200) {
                    if (!WARN_FIR) {
                        WARN_FIR = true;
                        mListener.onTemperatureWarnListener();
                    }
                } else if (event.values[0] < 900) {//防止临界点频繁切换
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

    @Override
    public boolean onMagneticOnKey(int keyCode, KeyEvent event) {
//        ToastShow.show(mContext,"门磁开");
        switch (keyCode) {
            case KeyEvent.KEYCODE_F1:
                countDoor1 = 0;
                MSG_MAGNETIC1_STATE = ACS_IO_ON;
                DOOR1_STATE = getIO(ACS_IO_OUT_DOOR1);
                if (DOOR1_STATE == ACS_IO_ON) {//门磁开，锁开，则关门
                    openDoor(false);
                }
                break;
            case KeyEvent.KEYCODE_F2:
                countDoor2 = 0;
                MSG_MAGNETIC2_STATE = ACS_IO_ON;
                DOOR2_STATE = getIO(ACS_IO_OUT_DOOR2);
                if (DOOR2_STATE == ACS_IO_ON) {//门磁开，锁开，则关门
                    openSecondDoor(false);
                }
//                else{//门磁开，锁关，针对门推开后，未关锁，则自动把锁缩回，但门磁关时，在锁
//                    openSecondDoor(true);
//                }
                break;
            case KeyEvent.KEYCODE_F3:
                break;
            case KeyEvent.KEYCODE_F4:
                break;
        }
        if (!isOnCountTime) {
            if (MSG_MAGNETIC1_STATE == ACS_IO_ON || MSG_MAGNETIC2_STATE == ACS_IO_ON) {
                //长时间未关门只告警一次，关门后重置
                isOnCountTime = true;
                closeMagneticCountDwon();
            }
        }
        return true;
    }

    @Override
    public boolean onMagneticOffKey(int keyCode, KeyEvent event) {
//        ToastShow.show(mContext,"门磁关");
        switch (keyCode) {
            case KeyEvent.KEYCODE_F1:
                MSG_MAGNETIC1_STATE = ACS_IO_OFF;
                DOOR1_STATE = getIO(ACS_IO_OUT_DOOR1);
                if (DOOR1_STATE == ACS_IO_ON) {
                    countDoor1++;
                    if (countDoor1 >= MAX_COUNT_DOOR) {
                        openDoor(false);
                        countDoor1 = 0;
                    }
                }
                break;
            case KeyEvent.KEYCODE_F2:
                MSG_MAGNETIC2_STATE = ACS_IO_OFF;
                DOOR2_STATE = getIO(ACS_IO_OUT_DOOR2);
                if (DOOR2_STATE == ACS_IO_ON) {
                    countDoor2++;
                    if (countDoor2 >= MAX_COUNT_DOOR) {
                        openSecondDoor(false);
                        countDoor2 = 0;
                    }
                }
                break;
            case KeyEvent.KEYCODE_F3:
                break;
            case KeyEvent.KEYCODE_F4:
                break;
        }
        if (isOnCountTime) {
            if (MSG_MAGNETIC1_STATE == ACS_IO_OFF && MSG_MAGNETIC2_STATE == ACS_IO_OFF) {
                isOnCountTime = false;
                startMagneticCountDwon();//关闭长时间未关门告警延迟
            }
        }
        return true;
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
                    mListener.overTimelistener();
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
