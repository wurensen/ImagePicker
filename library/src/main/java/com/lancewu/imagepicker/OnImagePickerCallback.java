package com.lancewu.imagepicker;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by LanceWu on 2019/1/30.<br/>
 * 图片选择回调
 */
public interface OnImagePickerCallback {

    /**
     * 打开选择器失败（找不到支持类型的Activity）
     */
    int ERROR_START_PICKER = 1;
    /**
     * 打开裁剪失败
     */
    int ERROR_START_CROP = 2;
    /**
     * 无SD卡写权限
     */
    int ERROR_NONE_WRITE_SD_PERMISSION = 3;
    /**
     * 取图返回处理失败
     */
    int ERROR_PICK_RESULT = 11;
    /**
     * 裁剪返回处理失败
     */
    int ERROR_CROP_RESULT = 12;

    @IntDef({ERROR_START_PICKER, ERROR_START_CROP, ERROR_NONE_WRITE_SD_PERMISSION,
            ERROR_PICK_RESULT, ERROR_CROP_RESULT})
    @Retention(RetentionPolicy.SOURCE)
    @interface ErrorCode {
    }

    /**
     * 发生错误时回调
     *
     * @param errorCode 错误码，参考{@link ErrorCode}包含的范围
     */
    void onPickError(@ErrorCode int errorCode);

    /**
     * 选择成功
     *
     * @param result 选择结果
     */
    void onPickSuccess(@NonNull ImagePickerResult result);

    /**
     * 取消选择
     */
    void onPickCancel();
}
