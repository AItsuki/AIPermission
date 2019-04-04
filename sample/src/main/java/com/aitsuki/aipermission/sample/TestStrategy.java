package com.aitsuki.aipermission.sample;

import android.content.DialogInterface;
import android.widget.Toast;

import com.aitsuki.aipermission.strategy.Request;
import com.aitsuki.aipermission.strategy.Strategy;

import androidx.appcompat.app.AlertDialog;

/**
 * Create by AItsuki on 2019/4/3.
 */
public class TestStrategy implements Strategy {

    @Override
    public void beforeRequest(final Request request) {
        request.request();
    }

    @Override
    public void onDenied(final Request request) {
        new AlertDialog.Builder(request.getCaller().requireContext())
                .setMessage(request.getRationale())
                .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.request();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(request);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onNoAskAgain(Request request) {
        finish(request);
        Toast.makeText(request.getCaller().requireContext(),
                "没有对应权限，请到设置中开启。", Toast.LENGTH_LONG).show();
    }

    private void finish(Request request) {
        request.getCaller().requireActivity().finish(); // 结束页面
    }
}
