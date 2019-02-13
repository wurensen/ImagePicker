package com.lancewu.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.lancewu.imagepicker.util.LogUtils;

import java.util.Arrays;

/**
 * Created by LanceWu on 2019/1/31.<br/>
 * 无界面Fragment，用于申请权限以及返回结果处理
 */
public class ImagePickerFragment extends Fragment {

    // 权限组
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    // 权限请求码
    private int mPermissionRequestCode;
    // 选择器
    private ImagePicker mImagePicker;
    // 回调
    private OnCheckPermissionCallback mCheckPermissionCallback;
    // 等关联到activity再检查权限
    private boolean mPendingCheck;

    /**
     * 关联fragment
     *
     * @param activity    界面
     * @param imagePicker 选择器
     * @return 如果已经添加直接返回，否则创建并添加
     */
    static ImagePickerFragment attach(Activity activity, ImagePicker imagePicker) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        String tag = "ImagePickerFragment";
        ImagePickerFragment fragment = (ImagePickerFragment) fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new ImagePickerFragment();
            fragment.setRetainInstance(true);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(fragment, tag);
            transaction.commitAllowingStateLoss();
        }
        fragment.mImagePicker = imagePicker;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mPendingCheck) {
            mPendingCheck = false;
            checkPermission(mPermissionRequestCode, mCheckPermissionCallback);
        }
    }

    /**
     * 检查权限
     *
     * @param requestCode 权限请求码
     * @param callback    回调
     */
    void checkPermission(int requestCode, OnCheckPermissionCallback callback) {
        mPermissionRequestCode = requestCode;
        mCheckPermissionCallback = callback;
        if (getActivity() == null) {
            mPendingCheck = true;
            return;
        }
        boolean granted = false;
        String deniedPermission = null;
        try {
            for (String permission : PERMISSIONS) {
                // 用兼容方法检查
                granted = ActivityCompat.checkSelfPermission(getActivity(), permission)
                        == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    deniedPermission = permission;
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.e("checkPermission", e);
        }
        if (granted) {
            notifyPermissionGranted();
            return;
        }
        // 6.0动态申请存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 申请WRITE_EXTERNAL_STORAGE权限
            boolean shouldShowRationale = false;
            for (String permission : PERMISSIONS) {
                shouldShowRationale = shouldShowRequestPermissionRationale(permission);
                if (shouldShowRationale) {
                    break;
                }
            }
            if (shouldShowRationale) {
                // 用户点过拒绝，未勾选不再询问
                LogUtils.d("shouldShowRequestPermissionRationale,just requestPermissions,requestCode="
                        + requestCode);
                requestPermissions(PERMISSIONS, requestCode);
                return;
            }
            // 系统可以显示该权限的申请弹窗,则向系统申请该权限
            LogUtils.d("requestPermissions,requestCode=" + requestCode);
            requestPermissions(PERMISSIONS, requestCode);
        } else {
            // 6.0以下无权限，直接回调无权限
            notifyPermissionDenied(deniedPermission);
        }
    }

    private void notifyPermissionGranted() {
        LogUtils.d("permission granted：" + Arrays.toString(PERMISSIONS) + ".VERSION=" + Build.VERSION.SDK_INT);
        if (mCheckPermissionCallback != null) {
            mCheckPermissionCallback.onGranted();
        }
    }

    private void notifyPermissionDenied(String... deniedPermissions) {
        LogUtils.d("permission denied:" + Arrays.toString(deniedPermissions) + ".VERSION=" + Build.VERSION.SDK_INT);
        if (mCheckPermissionCallback != null) {
            mCheckPermissionCallback.onDenied();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 判断存储权限
        if (requestCode != mPermissionRequestCode) {
            return;
        }
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    notifyPermissionDenied(permissions[i]);
                    return;
                }
            }
            // 权限申请通过
            notifyPermissionGranted();
        } else {
            notifyPermissionDenied(permissions);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 处理结果
        if (mImagePicker != null) {
            mImagePicker.dispatchResult(requestCode, resultCode, data);
        }
    }

    /**
     * 权限回调
     */
    interface OnCheckPermissionCallback {

        /**
         * 同意
         */
        void onGranted();

        /**
         * 拒绝
         */
        void onDenied();
    }
}
