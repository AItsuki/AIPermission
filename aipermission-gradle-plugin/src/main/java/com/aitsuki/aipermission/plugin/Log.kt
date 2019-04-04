package com.aitsuki.aipermission.plugin

import org.gradle.api.Project
import org.gradle.api.logging.Logger

/**
 * Create by AItsuki on 2019/3/29.
 */
object Log {

    private const val TAG = "AIPermission >>> "

    private var logger: Logger? = null

    fun initialize(project: Project) {
        this.logger = project.logger
    }

    fun lifeCycle(message: String) {
        logger?.lifecycle(TAG + message)
    }

    fun info(message: String) {
        logger?.info(TAG + message)
    }
}