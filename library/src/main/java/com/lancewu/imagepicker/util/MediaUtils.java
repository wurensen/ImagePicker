package com.lancewu.imagepicker.util;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

/**
 * Date 2017/12/27
 * 多媒体资源工具
 */
public class MediaUtils {

    /**
     * 从媒体的的uri获取图片的真实路径
     *
     * @return 图片真实路径
     */
    public static String getMediaRealPathFromUri(Context context, Uri localUri) {
        if (localUri == null) {
            return null;
        }
        String filepath = null;
        try {
            filepath = ContentResolverUtils.getRealPathFromUri(context, localUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果无法从路径中获取到图片的真实路径，则采用拷贝复制流的方式，生成临时的真实路径
        if (TextUtils.isEmpty(filepath)) {
            filepath = generateTempFilepath(context);
            boolean success = ContentResolverUtils.saveUriContent2File(context, localUri, filepath);
            if (success) {
                if (!new File(filepath).exists()) {
                    filepath = null;
                }
            } else {
                filepath = null;
            }
        }

        return filepath;
    }

    private static String generateTempFilepath(Context context) {
        // 保存到app的cache文件夹下
        return context.getExternalCacheDir() + File.separator + "image_picker_image_uri.jpg";
    }
}
