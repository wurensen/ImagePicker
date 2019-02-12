package com.lancewu.imagepicker.picker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lancewu.imagepicker.launcher.Launcher;
import com.lancewu.imagepicker.util.LogUtil;
import com.lancewu.imagepicker.util.MediaUtil;

import java.io.File;

/**
 * Created by LanceWu on 2019/1/29.<br/>
 * 图库选择器
 */
public class GalleryPicker implements Picker {

    // type，取图片类型
    private static final String INTENT_IMAGE_TYPE = "image/*";
    // 配置
    private PickerConfig mPickerConfig;

    @Override
    public boolean pick(@NonNull PickerConfig pickerConfig, @NonNull Launcher launcher) {
        mPickerConfig = pickerConfig;
        Activity activity = pickerConfig.getActivity();
        if (activity == null) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(INTENT_IMAGE_TYPE);
        ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
        if (componentName == null) {
            // 不存在对应类型的activity
            LogUtil.w("intent.resolveActivity return null! intent=" + intent);
            return false;
        }
        launcher.startActivityForResult(intent, pickerConfig.getRequestCode());
        return true;
    }

    @Nullable
    @Override
    public File onResult(Intent data) {
        if (data.getData() == null) {
            return null;
        }
        String content = "GalleryPicker pick data:" + data.getData().toString();
        try {
            // 取出文件真实路径
            String path = MediaUtil.getMediaRealPathFromUri(mPickerConfig.getActivity(), data.getData());
            if (path == null) {
                content += "\npath=null";
                return null;
            } else {
                content += "\npath=" + path;
                return new File(path);
            }
        } finally {
            LogUtil.d(content);
        }
    }
}
