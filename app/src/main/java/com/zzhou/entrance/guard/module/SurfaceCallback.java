package com.zzhou.entrance.guard.module;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.zzhou.entrance.guard.util.LogUtils;

public class SurfaceCallback implements SurfaceHolder.Callback {
    Camera mCamera;
    int numberOfCameras;
    int faceBackCameraId;
    int faceBackCameraOrientation;
    int faceFrontCameraId;
    int faceFrontCameraOrientation;
    int cameraId;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //有多少个摄像头
        numberOfCameras = Camera.getNumberOfCameras();
        LogUtils.d("摄像头有："+numberOfCameras+" 个");

        for (int i = 0; i < numberOfCameras; ++i) {
            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            Camera.getCameraInfo(i, cameraInfo);
            //后置摄像头
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                faceBackCameraId = i;
                faceBackCameraOrientation = cameraInfo.orientation;
            }
            //前置摄像头
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                faceFrontCameraId = i;
                faceFrontCameraOrientation = cameraInfo.orientation;
            }
        }
        cameraId = faceFrontCameraId;
        try {
            mCamera.setDisplayOrientation(180);
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);
        }catch (Exception e){
            LogUtils.d("摄像头被占用>>>>>>>>>>>>");
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != mCamera) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


}
