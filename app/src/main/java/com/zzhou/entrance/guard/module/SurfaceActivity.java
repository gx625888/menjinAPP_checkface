package com.zzhou.entrance.guard.module;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.zzhou.entrance.guard.R;
import com.zzhou.entrance.guard.util.LogUtils;

import java.io.IOException;

class SurfaceActivity extends Activity implements SurfaceHolder.Callback{
    private static final String TAG="CameraActivity";

    private Camera mCamera;
    private final int CAMERA_FRONT=1;//前置摄像头
    private final int CAMERA_BEHIND=0;//后置摄像头
    private int CAMERA_NOW=CAMERA_BEHIND;//默认打开后置摄像头


    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHoder;



    /////////////////////////////创建时/////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews(){//初始化控件
        mSurfaceView=findViewById(R.id.surface_view2);
        mSurfaceHoder=mSurfaceView.getHolder();
        mSurfaceHoder.addCallback(this);//添加回调接口
    }

    public Camera getCamera(){//初始化摄像头
        Camera camera;
        try {
            camera=Camera.open(CAMERA_NOW);//默认打开后置摄像头
            return camera;
        }catch (Exception e){
            LogUtils.d(TAG,"open camera failed");
            e.printStackTrace();
        }
        return null;
    }

    public void startPreview(Camera camera,SurfaceHolder surfaceHolder){
        if(camera==null){
            mCamera=getCamera();
        }else{
            mCamera=camera;
        }

        if(surfaceHolder==null){
            surfaceHolder=mSurfaceView.getHolder();
        }

        try {
            mCamera.setDisplayOrientation(90);//安卓默认是横屏，旋转转为90度，转为竖屏
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //创建时开始预览
        startPreview(mCamera,mSurfaceHoder);
    }

    //////////////////////////////改变时/////////////////////////////////
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    ////////////////////////////暂停时////////////////////////////////////

    @Override
    protected void onPause() {
        super.onPause();
    }

    /////////////////////////////恢复时///////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();
    }

    ////////////////////////////销毁时////////////////////////////////////
    public void releaseCamera(){
        mCamera.stopPreview();
        if(mCamera==null){
            return;
        }
        mCamera.release();
        mCamera=null;
        mSurfaceHoder=null;
    }
    @Override
    protected void onDestroy() {
        releaseCamera();
        super.onDestroy();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
