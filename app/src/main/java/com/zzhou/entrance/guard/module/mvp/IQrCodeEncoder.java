package com.zzhou.entrance.guard.module.mvp;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

interface IQrCodeEncoder {
    //最基本的方法
    Bitmap createQrCode(String content, int widthAndHeight);

    void createQrCode2ImageView(String content, ImageView imageView);

    //带图标（通过传入R资源/Drawable对象/Bitmap对象的图标）
    Bitmap createQrCode(String content, int width, int iconRes);

    Bitmap createQrCode(String content, int width, Drawable iconDrawable);

    Bitmap createQrCode(String content, int width, Bitmap iconBitmap);

    void createQrCode2ImageView(String content, ImageView imageView, int iconRes);

    void createQrCode2ImageView(String content, ImageView imageView, Drawable iconDrawable);

    void createQrCode2ImageView(String content, ImageView imageView, Bitmap iconBitmap);

    //手动设定是否带图标
    Bitmap createQrCode(String content, int width, int iconRes, boolean hasIcon);

    Bitmap createQrCode(String content, int width, Drawable iconDrawable, boolean hasIcon);

    Bitmap createQrCode(String content, int width, Bitmap iconBitmap, boolean hasIcon);

    void createQrCode2ImageView(String content, ImageView imageView, int iconRes, boolean hasIcon);

    void createQrCode2ImageView(String content, ImageView imageView, Drawable iconDrawable, boolean hasIcon);

    void createQrCode2ImageView(String content, ImageView imageView, Bitmap iconBitmap, boolean hasIcon);

    //将R资源转为Bitmap对象
    Bitmap getBitmapByRes(int resId);
    //将Drawable转为Bitmap对象
    Bitmap getBitmapByDrawable(Drawable drawable);
    //将Icon覆盖到二维码上并返回 带Icon的二维码
    Bitmap addIcon2QrCode(Bitmap icon, Bitmap qrCode);
}