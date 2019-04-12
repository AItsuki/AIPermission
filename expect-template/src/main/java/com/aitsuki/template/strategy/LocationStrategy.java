package com.aitsuki.template.strategy;

import android.content.Intent;

import com.aitsuki.aipermission.Host;
import com.aitsuki.aipermission.Request;
import com.aitsuki.aipermission.Strategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Create by AItsuki on 2019/4/10.
 */
public class LocationStrategy implements Strategy {

    @Override
    public void initialize(@NotNull Host host, @NotNull Request request) {

    }

    @Override
    public void onRequest() {

    }

    @Override
    public void onShowRationale() {

    }

    @Override
    public void onNeverAsk() {

    }

    @Override
    public void onDenied() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    }
}
