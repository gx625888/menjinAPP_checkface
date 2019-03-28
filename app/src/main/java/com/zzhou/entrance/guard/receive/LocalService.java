//package com.zzhou.entrance.guard.receive;
//
//import android.app.AlarmManager;
//import android.app.Service;
//import android.content.Intent;
//import android.hardware.Camera;
//import android.os.Binder;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.SurfaceView;
//import android.widget.Toast;
//
//import com.zzhou.entrance.guard.util.PhotoHandler;
//
//import java.io.IOException;
//
///**
// * <desc>
// * Created by The Moss on 2018/9/17.
// */
//
//
//public class LocalService extends Service {
//
//    private AlarmManager am = null;
//    private Camera camera;
//
//    private final IBinder mBinder = new LocalBinder();
//
//
//    /**
//     * Class for clients to access. Because we know this service always runs in
//     * the same process as its clients, we don't need to deal with IPC.
//     */
//    public class LocalBinder extends Binder {
//        public LocalService getService() {
//            return LocalService.this;
//        }
//
//    }
//
//    @Override
//    public void onCreate() {
//        init();
//    }
//
//    private void init() {
//        camera = openFacingBackCamera();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("LocalService", "Received start id " + startId + ": " + intent);
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        if (camera != null) {
//            camera.release();
//            camera = null;
//        }
//
//        Toast.makeText(this, "拍照服务关闭", Toast.LENGTH_SHORT)
//                .show();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    private void takePhoto(){
//        if (camera != null) {
//            SurfaceView dummy = new SurfaceView(getBaseContext());
//            try {
//                camera.setPreviewDisplay(dummy.getHolder());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            camera.startPreview();
//
//            camera.takePicture(null, null, new PhotoHandler(
//                    getApplicationContext()));
//        }
//    }
//
//    private Camera openFacingBackCamera() {
//        Camera cam = null;
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        ;
//        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
//            Camera.getCameraInfo(camIdx, cameraInfo);
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                try {
//                    cam = Camera.open(camIdx);
//                } catch (RuntimeException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return cam;
//    }
//}
