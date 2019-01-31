package com.lancewu.imagepicker.crop;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lancewu.imagepicker.launcher.Launcher;

import java.io.File;

/**
 * Created by LanceWu on 2019/1/30.<br/>
 * 裁剪接口
 */
public interface Crop {

    /**
     * 裁剪
     *
     * @param cropConfig 参数
     */
    boolean crop(@NonNull CropConfig cropConfig, @NonNull Launcher launcher);

    /**
     * 返回结果
     *
     * @param data 数据
     * @return 图片文件
     */
    @Nullable
    File onResult(Intent data);
}
