package com.aitsuki.aipermission.strategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Create by AItsuki on 2019/4/3.
 */
public interface Request {

    void request();

    /**
     * 如果是Activity，返回DelegateFragment。
     * 如果是Fragment，返回它。
     */
    Fragment getCaller();

    /**
     * 请求的宿主是否是Fragment，否则是Activity
     */
    boolean isFragment();

    /**
     * 申请的权限
     */
    @NonNull String[] getPermissions();

    int getRequestCode();

    @Nullable String getRationale();

    int getDeniedTime();
}
