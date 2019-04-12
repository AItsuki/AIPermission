package com.aitsuki.aipermission.strategy;

import android.widget.Toast;

/**
 * Create by AItsuki on 2019/4/3.
 */
public class DefaultStrategy implements Strategy {

    @Override
    public void beforeRequest(Request request) {
        request.request();
    }

    @Override
    public void onDenied(Request request) {
        request.request();
    }

    @Override
    public void onNoAskAgain(Request request) {
        // empty
        Toast.makeText(request.getCaller().requireContext(), "No permission!", Toast.LENGTH_SHORT).show();
    }
}
