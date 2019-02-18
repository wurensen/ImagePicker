package com.lancewu.imagepicker.picker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lancewu.imagepicker.launcher.Launcher;
import com.lancewu.imagepicker.provider.ImagePickerFileProvider;
import com.lancewu.imagepicker.util.LogUtils;

import java.io.File;

/**
 * Created by LanceWu on 2019/1/29.<br/>
 * 相机选择器，从相机获取照片
 */
public class CameraPicker implements Picker {

    // 拍照要保存的文件
    private Uri mTargetUri;

    @Override
    public boolean pick(@NonNull PickerConfig pickerConfig, @NonNull Launcher launcher) {
        Activity activity = pickerConfig.getActivity();
        if (activity == null) {
            return false;
        }
        // 启动相机程序
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 创建一个file，用来存储拍照后的照片
        File targetFile = getCameraTargetFile(pickerConfig);
        if (targetFile == null) {
            return false;
        }
        Uri uri;
        // 7.0共享文件需要采用FileProvider的方式以及权限声明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = ImagePickerFileProvider.getUriForFile(activity, targetFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(targetFile);
        }
        mTargetUri = uri;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.name());
        intent.putExtra("noFaceDetection", true);
        ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
        if (componentName == null) {
            // 不存在对应类型的activity
            LogUtils.w("intent.resolveActivity return null! intent=" + intent);
            return false;
        }
        LogUtils.d("CameraPicker start pick,targetFile=" + targetFile + ",uri=" + uri);
        launcher.startActivityForResult(intent, pickerConfig.getRequestCode());
        return true;
    }

    private File getCameraTargetFile(PickerConfig pickerConfig) {
        // 获取拍照保存的文件路径
        return pickerConfig.getImageFile();
    }

    @Nullable
    @Override
    public Uri onResult(Intent data) {
        // 直接返回图片
        return mTargetUri;
    }

}
