package com.kiva.ide.util;


import android.content.Context;
import android.os.Build;

/**
 * 
 * @author Kiva
 * @author Lody
 *
 */
public class AndroidHelper {

    public static int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean isFroyo() {
        return getSdkVersion() >= 8;
    }

    public static boolean isHoneycomb() {
        return getSdkVersion() >= 11;
    }

    /**
     * 屏幕适配平板用
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 3;
    }

    public static boolean isHoneycombTablet(Context context) {
        return isHoneycomb() && isTablet(context);
    }

}
