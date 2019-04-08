package com.zzhou.entrance.guard.serialPort;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class SerialPortActivity extends Activity{
    private static final String TAG = "SerialPortActivity";

    protected SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;

    private String prot = "/dev/ttySAC2";
    private int baudrate = 9600;
    private static long i = 0;

    private Toast mToast;


    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {

            }
        }
    };
    private Thread receiveThread;
    private Thread sendThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initLayout();

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    /**
     * 初始化Layout。
     */
    private void initLayout() {

    }

    private void openSerialPort(){
        // 打开
        try {
            mSerialPort = new SerialPort(new File(prot), baudrate, 0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
            receiveThread();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "打开失败");
            e.printStackTrace();
        }
    }

    private void sendSerialPort(){
        // 发送
        sendThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        i++;
                        mOutputStream.write((String.valueOf(i)).getBytes());
                        Log.i("test", "发送成功:1" + i);
                        Thread.sleep(1);
                    } catch (Exception e) {
                        Log.i("test", "发送失败");
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void receiveThread() {
        // 接收
        receiveThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    int size;
                    try {
                        byte[] buffer = new byte[1024];
                        if (mInputStream == null)
                            return;
                        size = mInputStream.read(buffer);
                        if (size > 0) {
                            String recinfo = new String(buffer, 0,
                                    size);
                            Log.i("test", "接收到串口信息:" + recinfo);
//                            sb = recinfo;
//                            handler.sendEmptyMessage(1);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        receiveThread.start();
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {

        if (mSerialPort != null) {
            mSerialPort.close();
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}