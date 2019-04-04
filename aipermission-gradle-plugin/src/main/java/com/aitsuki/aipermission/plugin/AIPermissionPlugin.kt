package com.aitsuki.aipermission.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Create by AItsuki on 2019/3/29.
 */
class AIPermissionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val hasApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (hasApp) {
            Scanner.initialize()
            Log.initialize(project)
            val android = project.extensions.getByType(AppExtension::class.java)
            android.registerTransform(AIPermissionTransform())
            Log.lifeCycle("Register AIPermissionPlugin!")
        }
    }
}