package com.aitsuki.processor

import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by AItsuki on 2019/4/9.
 */
class RequestCodeProvider {
    private val currentCode = AtomicInteger(0)

    fun nextRequestCode(): Int = currentCode.andIncrement
}