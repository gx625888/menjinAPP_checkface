package com.zzhou.entrance.guard.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.util.Log;

import com.zhangke.zlog.ZLog;
import com.zzhou.entrance.guard.AppConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;

/**
 * Created by zzhou on 10/12/16.
 */
public class LogUtils {
    public static boolean mLogEnable = true;
    public static boolean isWriter = false;
    private static String mClassname = LogUtils.class.getName();
    private static ArrayList<String> mMethods = new ArrayList();
    private static BufferedWriter writer;
    private static OutputStreamWriter osWriter;
    private static FileOutputStream fos;
    private static String logFilePath;
    private static SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");


    public LogUtils() {
    }

    public static void init(boolean logEnable) {
        mLogEnable = logEnable;
    }

    public static void d(String tag, String msg) {
        if (mLogEnable) {
//            Log.d(tag, getMsgWithLineNumber(msg));
            ZLog.d(tag,getMsgWithLineNumber(msg));
//            if (isWriter) {
//                write(getMsgWithLineNumber(msg), null);
//            }
        }

    }

    public static void e(String tag, String msg) {
        if (mLogEnable) {
//            Log.e(tag, getMsgWithLineNumber(msg));
            ZLog.e(tag,getMsgWithLineNumber(msg));
//            if (isWriter) {
//                write(getMsgWithLineNumber(msg), null);
//            }
        }

    }

    public static void i(String tag, String msg) {
        if (mLogEnable) {
//            Log.i(tag, getMsgWithLineNumber(msg));
            ZLog.i(tag,getMsgWithLineNumber(msg));
//            if (isWriter) {
//                write(getMsgWithLineNumber(msg), null);
//            }
        }

    }

    public static void w(String tag, String msg) {
        if (mLogEnable) {
//            Log.w(tag, getMsgWithLineNumber(msg));
            ZLog.wtf(tag,getMsgWithLineNumber(msg));
        }

    }

    public static void v(String tag, String msg) {
        if (mLogEnable) {
            Log.v(tag, getMsgWithLineNumber(msg));
        }

    }

    public static void d(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
//            Log.d(content[0], content[1]);
            ZLog.d(content[0], content[1]);
//            if (isWriter) {
//                write(content[1], null);
//            }
        }

    }

    public static void e(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
//            Log.e(content[0], content[1]);
            ZLog.e(content[0], content[1]);
//            if (isWriter) {
//                write(content[1], null);
//            }
        }

    }

    public static void i(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
//            Log.i(content[0], content[1]);
            ZLog.i(content[0], content[1]);
//            if (isWriter) {
//                write(content[1], null);
//            }
        }

    }

    public static void i() {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber("");
            Log.i(content[0], content[1]);
        }

    }

    public static void w(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.w(content[0], content[1]);
        }

    }

    public static void v(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.v(content[0], content[1]);
        }

    }

    public static String getMsgWithLineNumber(String msg) {
        try {
            StackTraceElement[] e = (new Throwable()).getStackTrace();
            int var2 = e.length;

            for (StackTraceElement st : e) {
                if (!mClassname.equals(st.getClassName()) && !mMethods.contains(st.getMethodName())) {
                    int b = st.getClassName().lastIndexOf(".") + 1;
                    String TAG = st.getClassName().substring(b);
                    return TAG + "->" + st.getMethodName() + "():" + st.getLineNumber() + "->" + msg;
                }
            }
        } catch (Exception var8) {
        }

        return msg;
    }

    public static String[] getMsgAndTagWithLineNumber(String msg) {
        try {
            StackTraceElement[] e = (new Throwable()).getStackTrace();
            int var2 = e.length;

            for (StackTraceElement st : e) {
                if (!mClassname.equals(st.getClassName()) && !mMethods.contains(st.getMethodName())) {
                    int b = st.getClassName().lastIndexOf(".") + 1;
                    String TAG = st.getClassName().substring(b);
                    String message = st.getMethodName() + "():" + st.getLineNumber() + "->" + msg;
                    return new String[]{TAG, message};
                }
            }
        } catch (Exception var9) {
        }

        return new String[]{"universal tag", msg};
    }

    static {
        Method[] ms = LogUtils.class.getDeclaredMethods();
        for (Method m : ms) {
            mMethods.add(m.getName());
        }
    }

    /**
     * 写文件操作
     *
     * @param msg       日志内容
     * @param throwable 异常捕获
     */
    private static final void write(String msg, Throwable throwable) {
        String timeStamp = FILE_NAME_FORMAT.format(Calendar.getInstance().getTime());
        try {
            writer.write(timeStamp);
            writer.write(" ");
            writer.write(msg);
            writer.newLine();
            writer.flush();
            osWriter.flush();
            fos.flush();
            if (throwable != null)
                saveCrash(throwable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存异常
     *
     * @param throwable
     * @throws IOException
     */
    private static void saveCrash(Throwable throwable) throws IOException {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);
        throwable.printStackTrace(pWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(pWriter);
            cause = cause.getCause();
        }
        pWriter.flush();
        pWriter.close();
        sWriter.flush();
        String crashInfo = writer.toString();
        sWriter.close();
        writer.write(crashInfo);
        writer.newLine();
        writer.flush();
        osWriter.flush();
        fos.flush();
    }

    /**
     * 日志组件初始化
     *
     * @param appCtx   application 上下文
     * @param isWriter 是否保存文件
     * @param level    日志级别
     */
    public static final void initialize(Context appCtx, boolean isWriter, Level level) {
        if (level == Level.OFF) {
            LogUtils.isWriter = false;
            return;
        }
        LogUtils.isWriter = isWriter;
        if (!LogUtils.isWriter) {//不保存日志到文件
            return;
        }
        String logFoldPath = AppConfig.getInstance().APP_PATH_ROOT + "/log";
//        pkgName = appCtx.getPackageName();
        File logFold = new File(logFoldPath);
        boolean flag = false;
        if (!(flag = logFold.exists()))
            flag = logFold.mkdirs();
        if (!flag) {
            LogUtils.isWriter = false;
            return;
        }
        logFilePath = logFoldPath + File.separator + FILE_NAME_FORMAT.format(Calendar.getInstance().getTime()) + ".txt";
        try {
            File logFile = new File(logFilePath);
            if (!(flag = logFile.exists()))
                flag = logFile.createNewFile();
            LogUtils.isWriter = isWriter & flag;
            if (LogUtils.isWriter) {
                fos = new FileOutputStream(logFile);
                osWriter = new OutputStreamWriter(fos);
                writer = new BufferedWriter(osWriter);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.isWriter = false;
        }
    }

}
