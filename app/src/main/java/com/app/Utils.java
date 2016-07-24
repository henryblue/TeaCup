package com.app;


import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
