package com.app.util;


import android.content.Context;
import android.util.DisplayMetrics;

public class ToolUtils {

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
