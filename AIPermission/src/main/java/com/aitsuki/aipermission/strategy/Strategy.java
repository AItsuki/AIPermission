package com.aitsuki.aipermission.strategy;

/**
 * Create by AItsuki on 2019/4/3.
 */
public interface Strategy {

    void beforeRequest(Request request);

    void onDenied(Request request);

    void onNoAskAgain(Request request);
}
