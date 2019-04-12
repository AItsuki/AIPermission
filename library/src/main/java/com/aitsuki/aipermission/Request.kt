package com.aitsuki.aipermission

/**
 * Create by AItsuki on 2019/4/10.
 */
interface Request {
    fun proceed()

    fun cancel()

    fun getPermissions(): Array<String>
}