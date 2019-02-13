package com.lancewu.imagepicker.picker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lancewu.imagepicker.launcher.Launcher;
import com.lancewu.imagepicker.provider.ImagePickerFileProvider;
import com.lancewu.imagepicker.util.LogUtils;
import com.lancewu.imagepicker.util.MediaUtils;

import java.io.File;

/**
 * Created by LanceWu on 2019/1/29.<br/>
 * 文档选择器
 */
public class DocumentPicker implements Picker {

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
        Intent intent;
        if (isUpAPI19()) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones).
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(INTENT_IMAGE_TYPE);
        } else {
            // 4.4以下使用 ACTION_GET_CONTENT
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(INTENT_IMAGE_TYPE);
        }
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
        if (isUpAPI19()) {
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            mPickerConfig.getActivity().getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
        }

        String content = "DocumentPicker pick data:" + imageUri.toString();
        try {
            // 取出文件真实路径
            String path = MediaUtils.getMediaRealPathFromUri(mPickerConfig.getActivity(), data.getData());
            if (path == null) {
                content += "\npath=null";
                return null;
            } else {
                content += "\npath=" + path;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return ImagePickerFileProvider.getUriForFile(mPickerConfig.getActivity(), new File(path));
                }
                return Uri.fromFile(new File(path));
            }
        } finally {
            LogUtils.d(content);
        }
    }

    private boolean isUpAPI19() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
