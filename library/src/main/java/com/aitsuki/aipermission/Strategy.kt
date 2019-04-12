package com.aitsuki.aipermission

import android.content.Intent

/**
 * Create by AItsuki on 2019/4/11.
 */
interface Strategy {

    fun initialize(host: Host, request: Request)

    fun onRequest()

    fun onShowRationale()

    fun onNeverAsk()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}

class DefaultStrategy : Strategy {

    private lateinit var host: Host
    private lateinit var request: Request

    override fun initialize(host: Host, request: Request) {
        this.host = host
        this.request = request
    }

    override fun onRequest() {
        request.proceed()
    }

    override fun onShowRationale() {
        request.proceed()
    }

    override fun onNeverAsk() {
        request.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // do nothing
    }
}