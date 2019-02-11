package com.lancewu.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.lancewu.imagepicker.crop.Crop;
import com.lancewu.imagepicker.crop.CropConfig;
import com.lancewu.imagepicker.crop.DefaultCrop;
import com.lancewu.imagepicker.launcher.FragmentLauncher;
import com.lancewu.imagepicker.launcher.Launcher;
import com.lancewu.imagepicker.picker.CameraPicker;
import com.lancewu.imagepicker.picker.GalleryPicker;
import com.lancewu.imagepicker.picker.Picker;
import com.lancewu.imagepicker.picker.PickerConfig;
import com.lancewu.imagepicker.util.LogUtil;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by LanceWu on 2019/1/29.<br/>
 * 系统图片选择器，封装从相机拍照/图库/文件管理器或任何支持了'image/*'类型的选择器选择图片，以及到系统裁剪的过程。<br/>
 */
public class ImagePicker {

    // 尽可能避免重复，requestCode从低16位的高位开始
    private static final int REQUEST_CODE_START = 0xffff;
    // 相机
    private static final int REQUEST_CODE_CAMERA = REQUEST_CODE_START - 1;
    // 图库
    private static final int REQUEST_CODE_GALLERY = REQUEST_CODE_START - 2;
    // 裁剪
    private static final int REQUEST_CODE_CROP = REQUEST_CODE_START - 3;
    // 权限
    private static final int REQUEST_CODE_PERMISSION = REQUEST_CODE_START - 4;
    // 选择器
    private Picker mPicker;
    // 选择配置
    private PickerConfig mPickerConfig;
    // 裁剪器
    private Crop mCrop;
    // 裁剪配置
    private CropConfig mCropConfig;
    // 回调
    private OnImagePickerCallback mCallback;
    // 界面调起者
    private Launcher mLauncher;
    // 请求码：打开请求权限
    private int mRequestCodePermission;

    private ImagePicker(Builder builder) {
        mRequestCodePermission = builder.mRequestCodePermission;
        boolean justCrop = false;
        switch (builder.mPickerType) {
            case Builder.CAMERA_PICKER:
                mPicker = new CameraPicker();
                mPickerConfig = new PickerConfig();
                mPickerConfig.setRequestCode(builder.mRequestCodeCamera);
                mPickerConfig.setActivity(builder.mActivity);
                mPickerConfig.setImageFile(builder.mCameraPickerSaveFile);
                break;
            case Builder.GALLERY_PICKER:
                mPicker = new GalleryPicker();
                mPickerConfig = new PickerConfig();
                mPickerConfig.setRequestCode(builder.mRequestCodeGallery);
                mPickerConfig.setActivity(builder.mActivity);
                break;
            case Builder.NONE_PICKER:
                justCrop = true;
                // 未设置选择器，也未设置裁剪
                if (builder.mCrop == null) {
                    throw new IllegalArgumentException("NONE_PICKER must call 'withCrop()'.");
                }
                break;
            default:
                throw new IllegalArgumentException("invalidate picker type:" + builder.mPickerType);
        }

        // 配置裁剪参数
        mCrop = builder.mCrop;
        if (mCrop != null) {
            mCropConfig = new CropConfig();
            mCropConfig.setRequestCode(builder.mRequestCodeCrop);
            mCropConfig.setActivity(builder.mActivity);
            // 配置裁剪参数
            CropConfigBuilder cropConfigBuilder = builder.mCropConfigBuilder;
            if (cropConfigBuilder == null) {
                throw new IllegalArgumentException("CropConfigBuilder must not be null.");
            }
            mCropConfig.setAspectX(cropConfigBuilder.mAspectX);
            mCropConfig.setAspectY(cropConfigBuilder.mAspectY);
            mCropConfig.setOutputX(cropConfigBuilder.mOutputX);
            mCropConfig.setOutputY(cropConfigBuilder.mOutputY);
            File outputImageFile = cropConfigBuilder.mOutputImageFile;
            if (outputImageFile == null) {
                throw new IllegalArgumentException("cropConfigBuilder's outputImageFile must not set null.");
            }
            mCropConfig.setOutputImageFile(outputImageFile);
            // 仅仅裁剪时，需要传入需要裁剪的图片文件
            if (justCrop) {
                File inputImageFile = cropConfigBuilder.mInputImageFile;
                if (inputImageFile == null) {
                    throw new IllegalArgumentException("CropConfigBuilder's inputImageFile must not be null.");
                }
                mCropConfig.setInputImageFile(inputImageFile);
            }
        }
    }

    /**
     * 发起选择/裁剪
     *
     * @param callback 回调
     */
    public void pick(OnImagePickerCallback callback) {
        if (mPicker == null && mCrop == null) {
            // 未设置选择器未设置裁剪
            throw new RuntimeException("none picker or crop!");
        }
        mCallback = callback;
        if (mPicker != null) {
            // 选图
            checkPermission(mPickerConfig.getActivity(), new ImagePickerFragment.OnCheckPermissionCallback() {
                @Override
                public void onGranted() {
                    if (!mPicker.pick(mPickerConfig, mLauncher)) {
                        notifyError(OnImagePickerCallback.ERROR_START_PICKER);
                    }
                }

                @Override
                public void onDenied() {
                    // 回调无权限
                    notifyError(OnImagePickerCallback.ERROR_NONE_WRITE_SD_PERMISSION);
                }
            });

        } else {
            // 裁剪
            checkPermission(mCropConfig.getActivity(), new ImagePickerFragment.OnCheckPermissionCallback() {
                @Override
                public void onGranted() {
                    if (!mCrop.crop(mCropConfig, mLauncher)) {
                        notifyError(OnImagePickerCallback.ERROR_START_CROP);
                    }
                }

                @Override
                public void onDenied() {
                    // 回调无权限
                    notifyError(OnImagePickerCallback.ERROR_NONE_WRITE_SD_PERMISSION);
                }
            });
        }
    }

    // 检查权限
    private void checkPermission(Activity activity, ImagePickerFragment.OnCheckPermissionCallback callback) {
        ImagePickerFragment pickerFragment = ImagePickerFragment.attach(activity, this);
        // 初始化启动器
        if (mLauncher == null) {
            mLauncher = new FragmentLauncher();
            ((FragmentLauncher) mLauncher).setFragment(pickerFragment);
        }
        pickerFragment.checkPermission(mRequestCodePermission, callback);
    }

    private void notifyError(@OnImagePickerCallback.ErrorCode int errorCode) {
        if (mCallback != null) {
            mCallback.onPickError(errorCode);
        }
    }

    private void notifySuccess(@NonNull File file) {
        if (mCallback != null) {
            mCallback.onPickSuccess(file);
        }
    }

    private void notifyCancel() {
        if (mCallback != null) {
            mCallback.onPickCancel();
        }
    }

    /**
     * 分发结果，在{@link Activity#onActivityResult(int, int, Intent)}调用
     *
     * @param requestCode 请求码
     * @param resultCode  请求结果
     * @param data        数据
     */
    void dispatchResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            LogUtil.d("cancel request!requestCode=" + requestCode + ",resultCode=" + resultCode);
            // 取消操作，回调
            if (mCallback != null) {
                mCallback.onPickCancel();
            }
            notifyCancel();
            return;
        }

        // 裁剪返回
        if (mCrop != null && mCropConfig.getRequestCode() == requestCode) {
            File file = mCrop.onResult(data);
            LogUtil.d("crop onResult,file=" + file);
            if (file == null) {
                // 选择失败，回调
                notifyError(OnImagePickerCallback.ERROR_CROP_RESULT);
                return;
            }
            notifySuccess(file);
            return;
        }

        // 选择器返回
        if (mPickerConfig.getRequestCode() == requestCode) {
            File file = mPicker.onResult(data);
            LogUtil.d("pick onResult,file=" + file);
            if (file == null) {
                // 选择失败，回调
                notifyError(OnImagePickerCallback.ERROR_PICK_RESULT);
                return;
            }
            // 开启裁剪
            if (mCrop != null) {
                mCropConfig.setInputImageFile(file); // 选择结果作为裁剪输入
                boolean crop = mCrop.crop(mCropConfig, mLauncher);
                if (!crop) {
                    notifyError(OnImagePickerCallback.ERROR_START_CROP);
                }
                return;
            }
            // 回调成功
            notifySuccess(file);
        }
    }

    /**
     * 实例构造器
     */
    public static class Builder {

        /**
         * 无，仅仅使用裁剪功能时可使用
         */
        static final int NONE_PICKER = 0;
        /**
         * 从相机拍照选择图片
         */
        static final int CAMERA_PICKER = 1;
        /**
         * 从图库（任何支持"image/*"的Activity）选择
         */
        static final int GALLERY_PICKER = 2;

        /**
         * 选择器类型输入限定
         */
        @IntDef({NONE_PICKER, CAMERA_PICKER, GALLERY_PICKER})
        @Retention(RetentionPolicy.SOURCE)
        @interface PICKER_TYPE {
        }

        // 选择器类型
        @PICKER_TYPE
        private int mPickerType = NONE_PICKER;
        // 选择完裁剪器
        private Crop mCrop;
        // 裁剪配置构造器
        private CropConfigBuilder mCropConfigBuilder;
        // 发起选图的界面
        private Activity mActivity;
        // 相机选择保存的图片
        private File mCameraPickerSaveFile;
        // 请求码：打开相机
        private int mRequestCodeCamera = REQUEST_CODE_CAMERA;
        // 请求码：打开图库
        private int mRequestCodeGallery = REQUEST_CODE_GALLERY;
        // 请求码：打开裁剪
        private int mRequestCodeCrop = REQUEST_CODE_CROP;
        // 请求码：打开请求权限
        private int mRequestCodePermission = REQUEST_CODE_PERMISSION;

        /**
         * 创建实例
         *
         * @param activity 发起选图的界面
         */
        public Builder(@NonNull Activity activity) {
            mActivity = activity;
        }

        /**
         * 从图库选择
         *
         * @return this
         */
        public Builder fromGallery() {
            return fromGallery(REQUEST_CODE_GALLERY);
        }

        /**
         * 从图库选择
         *
         * @param requestCode 开启图库页面请求码，出现请求码重复时可设置，默认值{@link #REQUEST_CODE_GALLERY}
         * @return this
         */
        public Builder fromGallery(int requestCode) {
            mPickerType = GALLERY_PICKER;
            mRequestCodeGallery = requestCode;
            return this;
        }

        /**
         * 从相机选择
         *
         * @param file 拍照的图片要存储的文件
         * @return this
         */
        public Builder fromCamera(@NonNull File file) {
            return fromCamera(file, REQUEST_CODE_CAMERA);
        }

        /**
         * 从相机选择
         *
         * @param file        拍照的图片要存储的文件
         * @param requestCode 开启相机页面请求码，出现请求码重复时可设置，默认值{@link #REQUEST_CODE_CAMERA}
         * @return this
         */
        public Builder fromCamera(@NonNull File file, int requestCode) {
            mPickerType = CAMERA_PICKER;
            mCameraPickerSaveFile = file;
            mRequestCodeCamera = requestCode;
            return this;
        }

        /**
         * 设置选择完照片需要裁剪，采用系统裁剪器
         *
         * @param cropConfigBuilder 裁剪参数构造器
         * @return this
         */
        public Builder withCrop(@NonNull CropConfigBuilder cropConfigBuilder) {
            if (!(mCrop instanceof DefaultCrop)) {
                mCrop = new DefaultCrop();
            }
            return withCrop(mCrop, cropConfigBuilder, mRequestCodeCrop);
        }

        /**
         * 设置选择完照片需要裁剪，采用自定义裁剪器
         *
         * @param crop              裁剪器
         * @param cropConfigBuilder 裁剪参数构造器
         * @param requestCode       开启裁剪页面请求码，出现请求码重复时可设置，默认值{@link #REQUEST_CODE_CROP}
         * @return this
         */
        public Builder withCrop(@NonNull Crop crop, @NonNull CropConfigBuilder cropConfigBuilder, int requestCode) {
            mCrop = crop;
            mCropConfigBuilder = cropConfigBuilder;
            mRequestCodeCrop = requestCode;
            return this;
        }

        /**
         * 设置权限请求码
         *
         * @param requestCode 开启权限请求码，出现请求码重复时可设置，默认值{@link #REQUEST_CODE_PERMISSION}
         * @return this
         */
        public Builder permissionRequestCode(int requestCode) {
            mRequestCodePermission = requestCode;
            return this;
        }

        /**
         * 构建选择器
         *
         * @return this
         */
        public ImagePicker build() {
            return new ImagePicker(this);
        }

    }

    /**
     * 裁剪配置参数构造器
     */
    public static class CropConfigBuilder {

        // 输入的图片文件
        private File mInputImageFile;
        // 裁剪后输出的图片文件
        private File mOutputImageFile;
        // 比例x
        private int mAspectX = 1;
        // 比例y
        private int mAspectY = 1;
        // 裁剪大小
        private int mOutputX = 200;
        // 裁剪大小
        private int mOutputY = 200;

        /**
         * 设置裁剪图片比例
         *
         * @param aspectX x
         * @param aspectY y
         * @return this
         */
        public CropConfigBuilder aspect(int aspectX, int aspectY) {
            mAspectX = aspectX;
            mAspectY = aspectY;
            return this;
        }

        /**
         * 设置裁剪输出图片大小
         *
         * @param outputX x(px)
         * @param outputY y(px)
         * @return this
         */
        public CropConfigBuilder outputSize(int outputX, int outputY) {
            mOutputX = outputX;
            mOutputY = outputY;
            return this;
        }

        /**
         * 设置裁剪源文件，仅在只做裁剪时设置才有效，否则都采用图片选择器的输出文件作为裁剪输入文件
         *
         * @param file 输入文件
         * @return this
         */
        public CropConfigBuilder inputFile(File file) {
            mInputImageFile = file;
            return this;
        }

        /**
         * 设置输出文件
         *
         * @param file 输出文件
         * @return this
         */
        public CropConfigBuilder outputFile(File file) {
            mOutputImageFile = file;
            return this;
        }

    }

}
