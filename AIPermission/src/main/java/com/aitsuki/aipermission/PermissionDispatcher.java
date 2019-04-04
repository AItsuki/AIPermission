package com.aitsuki.aipermission;

import android.content.pm.PackageManager;
import android.util.SparseArray;

import com.aitsuki.aipermission.delegate.DelegateFragment;
import com.aitsuki.aipermission.strategy.Strategy;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class PermissionDispatcher {

    private Fragment fragment;
    private SparseArray<Invoker> invokers = new SparseArray<>();


    public void bind(Fragment fragment) {
        this.fragment = fragment;
    }

    public void bind(FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        Fragment delegate = fm.findFragmentByTag("delegate");
        if (delegate == null) {
            delegate = new DelegateFragment();
            fm.beginTransaction()
                    .add(delegate, "delegate")
                    .commit();
        }
        ((DelegateFragment) delegate).bindDispatcher(this);
        this.fragment = delegate;
    }

    public void request(int requestCode,
                        String[] permissions,
                        String rationale,
                        Strategy strategy,
                        Runnable runnable) {
        if (checkSelfPermissions(permissions)) {
            runnable.run();
        } else {
            Invoker invoker = invokers.get(requestCode);
            if (invoker == null) {
                invoker = new Invoker(fragment, permissions, requestCode, rationale, strategy, runnable);
                invokers.put(requestCode, invoker);
            }
            invoker.getStrategy().beforeRequest(invoker);
        }
    }

    private boolean checkSelfPermissions(String[] permissions) {
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(fragment.requireContext(), permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void dispatchPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        Invoker invoker = invokers.get(requestCode);
        if (invoker != null) {
            checkActionResult(requestCode, invoker, permissions, grantResults);
        }
    }

    private void checkActionResult(int requestCode, Invoker invoker, String[] permissions, int[] results) {
        for (int i = 0; i < results.length; i++) {
            int result = results[i];
            // denied
            if (result == PackageManager.PERMISSION_DENIED) {
                if (fragment.shouldShowRequestPermissionRationale(permissions[i])) {
                    invoker.onDenied();
                    invoker.getStrategy().onDenied(invoker);
                } else {
                    // don't ask again
                    invoker.getStrategy().onNoAskAgain(invoker);
                }
                return;
            }
        }
        // grant
        invokers.remove(requestCode);
        invoker.invoke();
    }
}
