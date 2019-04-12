package com.aitsuki.template

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aitsuki.aipermission.DefaultStrategy
import com.aitsuki.aipermission.HostImpl
import com.aitsuki.aipermission.Request
import com.aitsuki.aipermission.Strategy
import com.aitsuki.aipermission.util.PermissionUtils
import java.lang.ref.WeakReference

/**
 * Create by AItsuki on 2019/4/12.
 */
private val OPENCAMERA_CODE = 1
private val OPENCAMERA_PERMISSIONS = arrayOf("android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE")
private val ACCESS_FINE_LOCATION = arrayOf("android.permission.ACCESS_FINE_LOCATION")

class MainActivityAIPermission(target: MainActivityArtifact): Request {

    private val targetRef = WeakReference<MainActivityArtifact>(target)
    private val strategy: Strategy = DefaultStrategy().also {
        it.initialize(HostImpl(target), this)
    }

    fun openCameraWithPermissionCheck() {
        val target = targetRef.get()?: return
        if (PermissionUtils.hasSelfPermissions(target, *OPENCAMERA_PERMISSIONS)) {
            target.openCamera()
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(target, *OPENCAMERA_PERMISSIONS)) {
                strategy.onShowRationale()
            } else {
//                AppCompatActivity.
            }
        }
    }

    override fun proceed() {
        val target = targetRef.get() ?: return
        ActivityCompat.requestPermissions(target, OPENCAMERA_PERMISSIONS, OPENCAMERA_CODE)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPermissions(): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}