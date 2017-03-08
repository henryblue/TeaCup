package com.app.teacup.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.app.teacup.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.app.Activity.RESULT_OK;

public class ToolUtils {

    private static final int[] mStyles = {R.style.AppTheme, R.style.greenTheme, R.style.pinkTheme,
            R.style.blackTheme, R.style.grayTheme, R.style.tealTheme, R.style.redTheme, R.style.purpleTheme};

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static int getWeatherImage(String weather) {
        switch (weather) {
            case "多云":
            case "多云转阴":
            case "多云转晴":
                return R.drawable.biz_plugin_weather_duoyun;
            case "中雨":
            case "中到大雨":
                return R.drawable.biz_plugin_weather_zhongyu;
            case "雷阵雨":
                return R.drawable.biz_plugin_weather_leizhenyu;
            case "阵雨":
            case "阵雨转多云":
                return R.drawable.biz_plugin_weather_zhenyu;
            case "暴雪":
                return R.drawable.biz_plugin_weather_baoxue;
            case "暴雨":
                return R.drawable.biz_plugin_weather_baoyu;
            case "大暴雨":
                return R.drawable.biz_plugin_weather_dabaoyu;
            case "大雪":
                return R.drawable.biz_plugin_weather_daxue;
            case "大雨":
            case "大雨转中雨":
                return R.drawable.biz_plugin_weather_dayu;
            case "雷阵雨冰雹":
                return R.drawable.biz_plugin_weather_leizhenyubingbao;
            case "晴":
                return R.drawable.biz_plugin_weather_qing;
            case "沙尘暴":
                return R.drawable.biz_plugin_weather_shachenbao;
            case "特大暴雨":
                return R.drawable.biz_plugin_weather_tedabaoyu;
            case "雾":
            case "雾霾":
                return R.drawable.biz_plugin_weather_wu;
            case "小雪":
                return R.drawable.biz_plugin_weather_xiaoxue;
            case "小雨":
                return R.drawable.biz_plugin_weather_xiaoyu;
            case "阴":
                return R.drawable.biz_plugin_weather_yin;
            case "雨夹雪":
                return R.drawable.biz_plugin_weather_yujiaxue;
            case "阵雪":
                return R.drawable.biz_plugin_weather_zhenxue;
            case "中雪":
                return R.drawable.biz_plugin_weather_zhongxue;
            default:
                return R.drawable.biz_plugin_weather_duoyun;
        }
    }

    public static String getTotalCacheSize(Context context) throws Exception {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return Formatter.formatFileSize(context, cacheSize);
    }

    private static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    private static byte [] getHash(String password) {
        MessageDigest digest = null ;
        try {
            digest = MessageDigest. getInstance( "SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        if (digest != null) {
            digest.reset();
            return digest.digest(password.getBytes());
        } else {
            return null;
        }

    }

    //SHA-256加密算法
    public static String SHA256Encrypt(String strForEncrypt) {
        byte [] data = getHash(strForEncrypt);
        return String.format( "%0" + (data.length * 2) + "X", new BigInteger(1, data)).toLowerCase();
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteRead;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ( (byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeFilePermission(File file) {
        try {
            String command = "chmod 666 " + file.getAbsolutePath();
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeToTheme(Activity activity, boolean isChange) {
        if (isChange) {
            Intent reIntent = new Intent();
            reIntent.putExtra("isChangeTheme", isChange);
            activity.setResult(RESULT_OK, reIntent);
        }
        activity.finish();
        Intent intent = new Intent(activity, activity.getClass());
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public static void onActivityCreateSetTheme(Context context) {
        SharedPreferences pf = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        int pos = pf.getInt("themePos", 0);
        context.setTheme(mStyles[pos]);
    }

    public static int getThemeColorPrimary(Context context){
        TypedValue typedValue = new  TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
}
