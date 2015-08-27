package com.kiva.ide.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * @author Lody
 */
public final class PixelDipConverter {

    private PixelDipConverter() {
    }

    /**
     * 把Dp转换为Px
     *
     * @param dp
     * @param context
     * @return
     */
    public static float convertDpToPixel(final float dp, final Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * metrics.densityDpi / 160f;
    }

    /**
     * 把Px转换为Dp
     *
     * @param px
     * @param context
     * @return
     */
    public static float convertPixelsToDp(final float px, final Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }
}
