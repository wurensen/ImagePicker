package com.lancewu.imagepicker.crop;

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
 * Created by LanceWu on 2019/1/30.<br/>
 * 系统自带裁剪器
 */
public class DefaultCrop implements Crop {

    // 裁剪action
    private static final String ACTION_CROP = "com.android.camera.action.CROP";
    // 裁剪文件Uri
    private Uri mOutputUri;

    @Override
    public boolean crop(@NonNull CropConfig cropConfig, @NonNull Launcher launcher) {
        Activity activity = cropConfig.getActivity();
        if (activity == null) {
            return false;
        }
        Intent intent;
        // 设置传入图片的uri
        Uri inputUri = cropConfig.getInputImageUri();
        if (inputUri == null) {
            File inputFile = cropConfig.getInputImageFile();
            if (inputFile == null) {
                return false;
            }
            LogUtils.d("DefaultCrop start crop,inputFile=" + inputFile);
            intent = new Intent(ACTION_CROP);
            // 7.0开始需要权限，uri需要适配
            if (isUpAPI24()) {
                inputUri = ImagePickerFileProvider.getUriForFile(activity, inputFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                inputUri = Uri.fromFile(inputFile);
            }
        } else {
            intent = new Intent(ACTION_CROP);
            if (isUpAPI24()) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
        intent.setDataAndType(inputUri, "image/*");

        // 设置裁剪后保存的图片uri
        File outputFile = cropConfig.getOutputImageFile();
        if (outputFile == null) {
            return false;
        }
        mOutputUri = Uri.fromFile(outputFile);
        // 输出到指定Uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputUri);
        // 是否保留比例
//        intent.putExtra("scale", false);
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", cropConfig.getAspectX());
        intent.putExtra("aspectY", cropConfig.getAspectY());
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", cropConfig.getOutputX());
        intent.putExtra("outputY", cropConfig.getOutputY());
        // 不需要直接返回图片，而是通过设置输出uri的方式
        intent.putExtra("return-data", false);
        // 去除默认的人脸识别，否则和剪裁匡重叠
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.name());
        ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
        if (componentName == null) {
            return false;
        }
        LogUtils.d("DefaultCrop start crop,inputUri=" + inputUri + "\noutputFile=" + outputFile);
        launcher.startActivityForResult(intent, cropConfig.getRequestCode());
        return true;
    }

    private boolean isUpAPI24() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    @Nullable
    @Override
    public Uri onResult(Intent data) {
        return mOutputUri;
    }
}
