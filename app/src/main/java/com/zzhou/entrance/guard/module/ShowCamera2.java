package com.zzhou.entrance.guard.module;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zzhou.entrance.guard.util.LogUtils;


public class ShowCamera2 extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holdMe;
    private Camera theCamera;
    private Context thecontext;

    public ShowCamera2(Context context,Camera camera) {
        super(context);
        theCamera = camera;
        holdMe = getHolder();
        thecontext = context;
        holdMe.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        //setCameraDisplayOrientation((Activity) thecontext);
        theCamera.setDisplayOrientation(180);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try   {
            theCamera.setPreviewDisplay(holder);
            theCamera.startPreview();
            LogUtils.d("展示相机");
        } catch (IOException e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {

        if (null != theCamera) {
            theCamera.stopPreview();
            theCamera.release();
            theCamera = null;
        }
        holdMe.removeCallback(this);
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
        theCamera.setDisplayOrientation(result);
    }

}
