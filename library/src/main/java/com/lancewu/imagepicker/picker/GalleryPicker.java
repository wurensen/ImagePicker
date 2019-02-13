package com.lancewu.imagepicker.picker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lancewu.imagepicker.launcher.Launcher;
import com.lancewu.imagepicker.util.LogUtils;

/**
 * Created by LanceWu on 2019/1/29.<br/>
 * 图库选择器
 */
public class GalleryPicker implements Picker {

    // type，取图片类型
    private static final String INTENT_IMAGE_TYPE = "image/*";

    @Override
    public boolean pick(@NonNull PickerConfig pickerConfig, @NonNull Launcher launcher) {
        Activity activity = pickerConfig.getActivity();
        if (activity == null) {
            return false;
        }
        Intent intent;
        intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, INTENT_IMAGE_TYPE);
        ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
        if (componentName == null) {
            // 不存在对应类型的activity
            LogUtils.w("intent.resolveActivity return null! intent=" + intent);
            return false;
        }
        launcher.startActivityForResult(intent, pickerConfig.getRequestCode());
        return true;
    }

    @Nullable
    @Override
    public Uri onResult(Intent data) {
        Uri imageUri = data.getData();
        if (imageUri == null) {
            return null;
        }
        String content = "GalleryPicker pick data:" + imageUri.toString();
        LogUtils.d(content);
        return imageUri;
    }

}
