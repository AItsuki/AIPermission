package com.aitsuki.aipermission.delegate;

import com.aitsuki.aipermission.PermissionDispatcher;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Create by AItsuki on 2019/3/28.
 */
public class DelegateFragment extends Fragment {

    private PermissionDispatcher dispatcher;

    public void bindDispatcher(PermissionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        dispatcher.dispatchPermissionResult(requestCode, permissions, grantResults);
    }
}
