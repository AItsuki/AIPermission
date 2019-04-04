package com.aitsuki.aipermission.plugin

import org.objectweb.asm.Type

/**
 * Create by AItsuki on 2019/4/2.
 */
data class ClassInfo(
        val parentPath: String = "",
        val path: String = "",
        val isJarEntry: Boolean = false,
        var className: String = "",
        var isFragment: Boolean = false,
        val methodsInfo: MutableList<MethodInfo> = mutableListOf()
)

data class MethodInfo(
        val name: String = "",
        val desc: String = "",
        var permissions: MutableList<String> = mutableListOf(),
        var rationale: String? = null,
        var rationaleId: Int? = null,
        var strategyType: Type? = null
)