package com.lancewu.imagepicker.picker;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lancewu.imagepicker.launcher.Launcher;

/**
 * Created by LanceWu on 2019/1/29.<br/>
 * 选择器
 */
public interface Picker {

    /**
     * 选择照片
     *
     * @param pickerConfig 参数
     * @param launcher     调起者
     */
    boolean pick(@NonNull PickerConfig pickerConfig, @NonNull Launcher launcher);

    /**
     * 返回结果
     *
     * @param data 数据
     * @return 图片文件Uri
     */
    @Nullable
    Uri onResult(Intent data);
}
