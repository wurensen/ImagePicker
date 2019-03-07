package com.lancewu.imagepickerdemo;

import android.app.Application;
import android.os.Process;
import android.util.Log;

/**
 * Created by LanceWu on 2019/3/7.<br/>
 */
public class ImagePickerApplication extends Application {

    public static final String TAG = "LanceWu-ImagePicker";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "ImagePickerApplication...onCreate(),pid=" + Process.myPid());
    }
}
