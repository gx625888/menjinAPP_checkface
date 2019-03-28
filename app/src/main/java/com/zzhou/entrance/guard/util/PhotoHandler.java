//package com.zzhou.entrance.guard.util;
//
//import android.content.Context;
//import android.hardware.Camera;
//import android.os.Environment;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
///**
// * <desc>
// * Created by The Moss on 2018/9/17.
// */
//
//public class PhotoHandler implements Camera.PictureCallback {
//
//    private final Context context;
//
//    public PhotoHandler(Context context) {
//        this.context = context;
//    }
//
//    @Override
//    public void onPictureTaken(byte[] data, Camera camera) {
//
//        File pictureFileDir = getDir();
//
//        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
//
//            Toast.makeText(context, "Can't create directory to save image.",
//                    Toast.LENGTH_LONG).show();
//            return;
//
//        }
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
//        String date = dateFormat.format(new Date());
//        String photoFile = "Picture_" + date + ".jpg";
//
//        String filename = pictureFileDir.getPath() + File.separator + photoFile;
//
//        File pictureFile = new File(filename);
//        System.out.println("filename is "+ filename);
//
//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(data);
//            fos.close();
//            Toast.makeText(context, "New Image saved:" + photoFile,
//                    Toast.LENGTH_LONG).show();
//        } catch (Exception error) {
//            Toast.makeText(context, "Image could not be saved.",
//                    Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private File getDir() {
//        File sdDir = Environment
//                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        return new File(sdDir, "ServiceCamera");
//    }
//}
