package com.lancewu.imagepicker.launcher;

import android.app.Fragment;
import android.content.Intent;

import com.lancewu.imagepicker.util.LogUtils;

/**
 * Created by LanceWu on 2019/1/31.<br/>
 * 通过fragment启动界面
 */
public class FragmentLauncher implements Launcher {

    private Fragment mFragment;

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (mFragment == null) {
            return;
        }
        LogUtils.d("startActivityForResult!requestCode=" + requestCode);
        mFragment.startActivityForResult(intent, requestCode);
    }
}
