package com.lancewu.imagepicker.util;

import android.util.Log;

import com.lancewu.imagepicker.BuildConfig;

/**
 * Created by LanceWu on 2019/1/30.<br/>
 * 调试工具
 */
public class LogUtils {

    private static final String TAG = "ImagePicker";

    private static boolean sEnable = BuildConfig.DEBUG;

    public static void d(String message) {
        if (sEnable) {
            Log.d(TAG, message);
        }
    }

    public static void w(String message) {
        if (sEnable) {
            Log.w(TAG, message);
        }
    }

    public static void e(String message) {
        if (sEnable) {
            Log.e(TAG, message);
        }
    }

    public static void e(String message, Throwable throwable) {
        if (sEnable) {
            Log.e(TAG, message, throwable);
        }
    }
}
