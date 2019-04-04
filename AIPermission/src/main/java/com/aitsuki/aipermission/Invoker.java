package com.aitsuki.aipermission;

import com.aitsuki.aipermission.delegate.DelegateFragment;
import com.aitsuki.aipermission.strategy.Request;
import com.aitsuki.aipermission.strategy.Strategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Create by AItsuki on 2019/4/3.
 */
public class Invoker implements Request {

    private Fragment fragment;
    private String[] permissions;
    private int requestCode;
    private int deniedTime;
    private Runnable runnable;
    private String rationale;
    private Strategy strategy;

    Invoker(@NonNull Fragment fragment, @NonNull String[] permissions, int requestCode,
            @Nullable String rationale, @Nullable Strategy strategy, @NonNull Runnable runnable) {
        this.fragment = fragment;
        this.permissions = permissions;
        this.requestCode = requestCode;
        this.rationale = rationale;
        this.strategy = strategy;
        this.runnable = runnable;
    }

    @Override
    public void request() {
        fragment.requestPermissions(permissions, requestCode);
    }

    @Override
    public Fragment getCaller() {
        return fragment;
    }

    @Override
    public boolean isFragment() {
        return fragment instanceof DelegateFragment;
    }

    @NonNull
    @Override
    public String[] getPermissions() {
        return permissions;
    }

    @Override
    public int getRequestCode() {
        return requestCode;
    }

    @Override
    @Nullable
    public String getRationale() {
        return rationale;
    }

    @Override
    public int getDeniedTime() {
        return deniedTime;
    }

    void onDenied() {
        deniedTime++;
    }

    void invoke() {
        runnable.run();
    }

    Strategy getStrategy() {
        return strategy;
    }
}
