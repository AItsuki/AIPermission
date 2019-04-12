package com.aitsuki.aipermission

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

/**
 * Create by AItsuki on 2019/4/10.
 */
interface Host {
    fun isFragment(): Boolean

    fun getFragment(): Fragment?

    fun getActivity(): Activity?

    fun getContext(): Context?

    fun startActivity(intent: Intent)

    fun startActivityForResult(intent: Intent, requestCode: Int)

    fun finish()
}

class HostImpl() : Host {

    private var activityRef: WeakReference<Activity>? = null
    private var fragmentRef: WeakReference<Fragment>? = null
    private var isFragment: Boolean = false

    constructor(fragment: Fragment) : this() {
        this.fragmentRef = WeakReference(fragment)
        isFragment = true
    }

    constructor(activity: Activity) : this() {
        this.activityRef = WeakReference(activity)
        isFragment = false
    }

    override fun isFragment() = isFragment

    override fun getFragment() = fragmentRef?.get()

    override fun getActivity() = activityRef?.get()

    override fun getContext() =
            if (isFragment) getFragment()?.requireContext() else getActivity()

    override fun startActivity(intent: Intent) {
        getContext()?.startActivity(intent)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        if (isFragment) {
            getFragment()?.startActivityForResult(intent, requestCode)
        } else {
            getActivity()?.startActivityForResult(intent, requestCode)
        }
    }

    override fun finish() {
        if (isFragment) {
            getFragment()?.activity?.finish()
        } else {
            getActivity()?.finish()
        }
    }
}