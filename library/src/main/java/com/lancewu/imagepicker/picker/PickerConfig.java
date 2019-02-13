package com.lancewu.imagepicker.picker;

import android.app.Activity;

import java.io.File;

/**
 * Created by LanceWu on 2019/1/29.<br/>
 * 配置参数
 */
public class PickerConfig {

    // 来源页面
    private Activity mActivity;
    // 请求码
    private int mRequestCode;
    // 图片文件
    private File mImageFile;

    public Activity getActivity() {
        return mActivity;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    public File getImageFile() {
        return mImageFile;
    }

    public void setImageFile(File imageFile) {
        mImageFile = imageFile;
    }
}
