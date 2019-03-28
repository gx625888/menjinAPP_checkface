package com.zzhou.entrance.guard.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <desc>
 * Created by The Moss on 2018/11/13.
 */

public class RandomPassUtils {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("mmHHddMMmm");
    public static String randomPass(){

        return null;
    }
    public static void main(String[] args) {
        String arg = dateFormat.format(new Date());
        System.out.println(arg);
        System.out.println(2%3);
    }
}
