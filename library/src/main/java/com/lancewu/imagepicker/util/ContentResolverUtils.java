package com.lancewu.imagepicker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * ContentResolver工具类
 *
 * @author JJ
 * @since 13-4-11 下午2:48
 */
public class ContentResolverUtils {

    /**
     * 保存Uri里面的内容到本地文件
     *
     * @param pContext     上下文
     * @param pSrcUri      待保存uri路径
     * @param pDstFilePath 保存的真实路径地址
     * @return true保存成功，false保存失败
     * @see #saveUriContent2File(Context, Uri, String)
     */
    public static boolean saveUriContent(Context pContext, Uri pSrcUri, String pDstFilePath) {
        return saveUriContent2File(pContext, pSrcUri, pDstFilePath);
    }

    /**
     * 保存Uri里面的内容到本地文件
     *
     * @param pContext      上下文
     * @param pSrcUri       待保存uri路径
     * @param pDestFilepath 保存的真实路径地址
     * @return true保存成功，false保存失败
     */
    public static boolean saveUriContent2File(Context pContext, Uri pSrcUri, String pDestFilepath) {
        boolean isSuccess = false;
        try {
            isSuccess = StreamUtils.copyAndClose(pContext.getContentResolver().openInputStream(pSrcUri),
                    new FileOutputStream(pDestFilepath, false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    /**
     * 从URI中获取图片真实路径
     *
     * @param pContext 上下文
     * @param uri      文件所在的uri
     * @return 最终路径
     */
    public static String getRealPathFromUri(Context pContext, Uri uri) {
        if (null == uri) {
            return null;
        }
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return null;
        }

        if (uri.toString().startsWith("content://com.android.providers")) {// 4.4以上的uri格式
            // :content://com.android.providers.media.documents/document/image%3A137638
            return resolveDocumentProviderUri(pContext, uri);
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {// file:///storage/emulated/0/Android/data/com.dropbox.android/files/scratch/go.png
            String uriStr = uri.toString();
            int index = "file://".length() + 1;
            if (uriStr.length() > index) {
                return uriStr.substring(index);
            }
        } else if (uri.toString().startsWith("content://media")) {// content://media/external/images/media/65360
            return resolveMediaUri(pContext, uri);
        }
        return null;
    }

    /**
     * 解析Document Provider的图片资源，返回uri指向的文件的真实绝对路径
     *
     * @param context 上下文
     * @param uri     文件所在的uri
     * @return 最终路径
     */
    private static String resolveDocumentProviderUri(Context context, Uri uri) {
        String result = null;
        Cursor cursor = null;
        try {
            // 获得资源唯一ID
            String temp = uri.toString();
            int lastSlash = temp.lastIndexOf("%");
            String subTemp = temp.substring(lastSlash + 1, temp.length());
            lastSlash = subTemp.lastIndexOf("A");
            String wholeID = subTemp.substring(lastSlash + 1, subTemp.length());
            // 定义索引字段
            String[] selectionArgs = {wholeID};
            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, "_id = ?",
                    selectionArgs, null);
            if (cursor != null) {
                int DATA_COLUMN_INDEX = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    // DATA字段就是本地资源的全路径
                    result = cursor.getString(DATA_COLUMN_INDEX);
                }
            } else {
                result = "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // 切记要关闭游标
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 从URI中获取图片真实路径
     *
     * @param pContext   上下文
     * @param contentUri 文件所在的uri
     * @return 最终路径
     */
    private static String resolveMediaUri(Context pContext, Uri contentUri) {
        if (null == contentUri) {
            return null;
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = pContext.getContentResolver().query(contentUri, // 内容的uri
                    projection, // Which columns to return
                    null, // WHERE clause; which rows to return (all rows)
                    null, // WHERE clause selection arguments (none)
                    null); // Order-by clause (ascending by name)
            String finalFilePath = null;
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                if (cursor.getCount() != 0) {
                    finalFilePath = cursor.getString(column_index);
                }
            } else {
                finalFilePath = contentUri.getPath();
            }

            return finalFilePath;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

}
