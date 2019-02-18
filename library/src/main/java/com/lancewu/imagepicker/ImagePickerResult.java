package com.lancewu.imagepicker;

import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by LanceWu on 2019/2/12.<br/>
 * 选图结果
 */
public class ImagePickerResult {
    // 图片Uri
    private Uri mImageUri;

    ImagePickerResult(Uri imageUri) {
        mImageUri = imageUri;
    }

    /**
     * 获取图片Uri
     * @return Uri
     */
    @NonNull
    public Uri getImageUri() {
        return mImageUri;
    }
}
