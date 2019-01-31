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
import com.lancewu.imagepicker.util.LogUtil;

import java.io.File;

/**
 * Created by LanceWu on 2019/1/30.<br/>
 * 系统自带裁剪器
 */
public class DefaultCrop implements Crop {

    // 裁剪文件
    private File mCropFile;

    @Override
    public boolean crop(@NonNull CropConfig cropConfig, @NonNull Launcher launcher) {
        Activity activity = cropConfig.getActivity();
        if (activity == null) {
            return false;
        }
        File inputFile = cropConfig.getInputImageFile();
        if (inputFile == null) {
            return false;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        // 设置传入图片的uri
        Uri inputUri;
        // 7.0开始需要权限，uri需要适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            inputUri = ImagePickerFileProvider.getUriForFile(activity, inputFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            inputUri = Uri.fromFile(inputFile);
        }
        intent.setDataAndType(inputUri, "image/*");

        // 设置裁剪后保存的图片uri
        mCropFile = cropConfig.getOutputImageFile();
        File outputFile = mCropFile;
        if (outputFile == null) {
            return false;
        }
        Uri outputUri = Uri.fromFile(outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
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
        LogUtil.d("DefaultCrop start crop,inputFile=" + inputFile + ",inputUri=" + inputUri);
        LogUtil.d("DefaultCrop start crop,outputFile=" + outputFile + ",outputUri=" + outputUri);
        launcher.startActivityForResult(intent, cropConfig.getRequestCode());
        return true;
    }

    @Nullable
    @Override
    public File onResult(Intent data) {
        return mCropFile;
    }
}
