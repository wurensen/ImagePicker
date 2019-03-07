package com.lancewu.imagepicker.result;

import android.content.Intent;

/**
 * Created by LanceWu on 2019/3/7.<br/>
 * 结果分发
 */
public interface ResultDispatcher {

    /**
     * 分发
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        数据
     */
    void dispatch(int requestCode, int resultCode, Intent data);
}
