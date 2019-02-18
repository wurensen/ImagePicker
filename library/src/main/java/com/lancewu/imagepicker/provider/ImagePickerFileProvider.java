package com.lancewu.imagepicker.provider;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.lancewu.imagepicker.util.LogUtils;

import java.io.File;

/**
 * Created by LanceWu on 2019/1/30.<br/>
 * 文件Provider
 */
public class ImagePickerFileProvider extends FileProvider {

    /**
     * 获取Authority
     *
     * @return String
     */
    public static String getAuthority() {
        return "com.lancewu.imagepicker.ImagePickerFileProvider";
    }

    /**
     * 获取通过自定义FileProvider文件的Uri
     *
     * @param context 上下文
     * @param file    文件
     * @return Uri对象
     */
    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        try {
            return FileProvider.getUriForFile(context, getAuthority(), file);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("getUriForFile:" + file, e);
        }
        return Uri.EMPTY;
    }
}
