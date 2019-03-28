//package com.zzhou.entrance.guard.widget;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.annotation.StringRes;
//import android.util.DisplayMetrics;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.wang.avi.AVLoadingIndicatorView;
//import com.zzhou.entrance.guard.R;
//import com.zzhou.entrance.guard.util.LogUtils;
//
///**
// * <desc>
// * Created by The Moss on 2018/10/31.
// */
//
//public class CustomDialog extends Dialog {
//    TextView mContent;
//    AVLoadingIndicatorView mAvi;
//
//    static CustomDialog instance;
//
//    public CustomDialog(@NonNull Context context) {
//        this(context, R.style.MyDialogStyleTop);
//    }
//
//    public CustomDialog(@NonNull Context context, int themeResId) {
//        super(context, R.style.MyDialogStyleTop);
//        init(context);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//    private void init(Context context){
//        DisplayMetrics dm = context.getResources().getDisplayMetrics();
//        int height = dm.heightPixels;
//        int width = dm.widthPixels;
//        //设置dialog的宽高为屏幕的宽高
//        ViewGroup.LayoutParams layoutParams = new  ViewGroup.LayoutParams(width - 48,height - 48);
//        View view = LayoutInflater.from(context).inflate(R.layout.dialog_customer, null);
//        setContentView(view,layoutParams);
//        mContent = view.findViewById(R.id.dialog_content);
//        mAvi = view.findViewById(R.id.dialog_avi);mContent = view.findViewById(R.id.dialog_content);
//        mAvi = view.findViewById(R.id.dialog_avi);
//    }
//    public static CustomDialog create(Context context){
//        if (instance == null) {
//            synchronized (CustomDialog.class){
//                if (instance == null) {
//                    instance = new CustomDialog(context);
//                }
//            }
//        }
//        return instance;
//    }
//    @Override
//    public void setOnDismissListener(@Nullable OnDismissListener listener) {
//        hideAvi();
//        super.setOnDismissListener(listener);
//    }
//    public CustomDialog setContent(String msg){
//        mContent.setText(msg);
//        return instance;
//    }
//    public CustomDialog setContent(@StringRes int resId){
//        mContent.setText(resId);
//        return instance;
//    }
//    public CustomDialog showAvi(){
//        mAvi.show();
//        return instance;
//    }
//    public CustomDialog hideAvi(){
//        mAvi.hide();
//        return instance;
//    }
//
//    @Override
//    public void dismiss() {
//        super.dismiss();
//        LogUtils.d("隐藏弹框");
//        hideAvi();
//    }
//
//    @Override
//    public void show() {
//        LogUtils.d("显示弹框");
//        super.show();
//    }
//}
