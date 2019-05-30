package com.zzhou.entrance.guard.module;

import android.app.Activity;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;

import com.zzhou.entrance.guard.MyApplication;
import com.zzhou.entrance.guard.util.LogUtils;

import java.io.IOException;

import static android.os.Looper.getMainLooper;
import static com.zzhou.entrance.guard.netty.NettyService.TAG;

public class SurfaceCallback implements SurfaceHolder.Callback {
    Camera mCamera;
    AssistStructure.ViewNode mSurfaceView;
    int mOrienta;
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
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);
        }catch (Exception e){
            LogUtils.d("摄像头被占用1111111");
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            //initCamera(mSurfaceView.getWidth(),mSurfaceView.getHeight());
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

    public void setCameraDisplayOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(1, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        LogUtils.d("摄像头被旋转的角度;"+ result);
        mOrienta = result;//该值有其它用途
        mCamera.setDisplayOrientation(result);
    }


}
