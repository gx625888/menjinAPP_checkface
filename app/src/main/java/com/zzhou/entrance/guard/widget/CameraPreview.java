package com.zzhou.entrance.guard.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zzhou.entrance.guard.util.LogUtils;

import java.io.IOException;

/**
 * <desc>
 * Created by The Moss on 2018/9/20.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera myCamera;

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void takePhoto(Camera.PictureCallback pictureCallback){
        if (myCamera == null) {
            LogUtils.d(">>>>>>>>>>>>>>takePhoto myCamera == null reset camera<<<<<<<<<<<<<<<<<");
//                return;
            initCamera();
        }
        if (myCamera == null) {
            LogUtils.d(">>>>>>>>>>>>>>takePhoto myCamera == null<<<<<<<<<<<<<<<<<");
            return;
        }
        myCamera.startPreview();
        myCamera.takePicture(null, null, pictureCallback);
    }
    public void init(){
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //一直保持屏幕常亮
        mHolder.setKeepScreenOn(true);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        try {
            if (myCamera == null) {
                LogUtils.d(">>>>>>>>>>>>>>相机 myCamera == null reset camera<<<<<<<<<<<<<<<<<");
//                return;
                initCamera();
            }
            if (myCamera == null) {
                LogUtils.d(">>>>>>>>>>>>>>相机 myCamera == null<<<<<<<<<<<<<<<<<");
                return;
            }
            myCamera.enableShutterSound(false);
            //这里的myCamera为已经初始化的Camera对象
            myCamera.setPreviewDisplay(holder);
            LogUtils.d(">>>>>>>>>>>>>>相机 surfaceChanged<<<<<<<<<<<<<<<<<");
        } catch (IOException e) {
            LogUtils.e(">>>>>>>>>>>>>>相机 e " +e.getMessage());
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
            return;
        }
        myCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        //开启相机
        LogUtils.d(">>>>>>>>>>>>>>开启相机<<<<<<<<<<<<<<<<<");
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        if (myCamera != null) {
            //关闭预览并释放资源
            LogUtils.d("CameraPreview", "surfaceDestroyed");
            myCamera.setPreviewCallback(null);
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
    }
    public void onPause(){
        this.getHolder().removeCallback(this);
    }
    //初始化摄像头
    public void initCamera() {
        //如果存在摄像头
        if (checkCameraHardware(getContext())) {
            //获取摄像头（首选前置，无前置选后置）
            if (openFacingFrontCamera()) {
                LogUtils.d("CameraPreview", "openCameraSuccess");
            } else {
                LogUtils.d("CameraPreview", "openCameraFailed");
            }

        }
    }

    //判断是否存在摄像头
    private boolean checkCameraHardware(Context context) {

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // 设备存在摄像头
            return true;
        } else {
            // 设备不存在摄像头
            return false;
        }

    }

    //得到后置摄像头
    private boolean openFacingFrontCamera() {

        //尝试开启前置摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    myCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
//                    e.printStackTrace();
                    LogUtils.e("开启后置摄像头异常,e = " +e.getMessage());
                    return false;
                }
            }
        }

        //如果开启前置失败（无前置）则开启后置
        if (myCamera == null) {
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        myCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        LogUtils.e("开启前置摄像头异常,e = " +e.getMessage());
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
