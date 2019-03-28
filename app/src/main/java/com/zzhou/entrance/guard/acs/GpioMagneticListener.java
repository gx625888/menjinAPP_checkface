package com.zzhou.entrance.guard.acs;

import android.view.KeyEvent;

/**
 * <desc>
 * Created by The Moss on 2018/9/28.
 */

public interface GpioMagneticListener {
    boolean onMagneticOnKey(int keyCode, KeyEvent event);
    boolean onMagneticOffKey(int keyCode, KeyEvent event);
}
