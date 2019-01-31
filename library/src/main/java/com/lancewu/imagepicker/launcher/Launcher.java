package com.lancewu.imagepicker.launcher;

import android.content.Intent;

/**
 * Created by LanceWu on 2019/1/31.<br/>
 * 外部activity启动器
 */
public interface Launcher {

    /**
     * 开启页面
     *
     * @param intent      意图
     * @param requestCode 请求码
     */
    void startActivityForResult(Intent intent, int requestCode);
}
