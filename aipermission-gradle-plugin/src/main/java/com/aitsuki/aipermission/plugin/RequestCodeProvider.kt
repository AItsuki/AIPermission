package com.aitsuki.aipermission.plugin

import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by AItsuki on 2019/4/2.
 */
object RequestCodeProvider {

    private var requestCode = AtomicInteger(1)

    // requestCode是short类型，不能超过short最大值
    fun get(): Int {
        requestCode.compareAndSet(Short.MAX_VALUE.toInt(), 1)
        return requestCode.getAndIncrement()
    }
}