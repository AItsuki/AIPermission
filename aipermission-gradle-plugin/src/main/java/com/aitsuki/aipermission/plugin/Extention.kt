package com.aitsuki.aipermission.plugin

import java.io.File

/**
 * Create by AItsuki on 2019/3/29.
 */
fun String.toSlash(): String = if (File.separator == "/") this else replace("\\", "/")
