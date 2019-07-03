package com.zzhou.entrance.guard.module;

import java.io.IOException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zzhou.entrance.guard.R;
import com.zzhou.entrance.guard.util.LogUtils;

public class ShowCamera extends Activity implements SurfaceHolder.Callback {

private SurfaceView surfaceview;
private SurfaceHolder surfaceholder;
private Camera camera = null;
        Camera.Parameters parameters;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_surface);

        LogUtils.d("<<<<<ShowCamera>>>>>");
        surfaceview = (SurfaceView) findViewById(R.id.surface_view1);
        surfaceholder = surfaceview.getHolder();
        surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceholder.addCallback(this);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // TODO Auto-generated method stub

                camera.autoFocus(new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                initCamera();// 实现相机的参数初始化
                camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                }
                }

                });
                }

@Override
public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // 获取camera对象
        camera = Camera.open();
        try {
        // 设置预览监听
        camera.setPreviewDisplay(holder);
        Camera.Parameters parameters = camera.getParameters();

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
        parameters.set("orientation", "portrait");
        camera.setDisplayOrientation(90);
        parameters.setRotation(90);
        } else {
        parameters.set("orientation", "landscape");
        camera.setDisplayOrientation(0);
        parameters.setRotation(0);
        }
        camera.setParameters(parameters);
        // 启动摄像头预览
        camera.startPreview();
        System.out.println("camera.startpreview");

        } catch (IOException e) {
        e.printStackTrace();
        camera.release();
        System.out.println("camera.release");
        }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        if (camera != null) {
        camera.stopPreview();
        camera.release();
        }
        }

        // 相机参数的初始化设置
        private void initCamera() {
        LogUtils.d("<<<<<ShowCamera-initCamera>>>>>");
        parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        // parameters.setPictureSize(surfaceView.getWidth(),
        // surfaceView.getHeight()); // 部分定制手机，无法正常识别该方法。
        //parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
        setDispaly(parameters, camera);
        camera.setParameters(parameters);
        camera.startPreview();
        camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上

        }

        // 控制图像的正确显示方向
        private void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
        setDisplayOrientation(camera, 180);
        } else {
        parameters.setRotation(180);
        }

        }

        // 实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[] { i });
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }
}