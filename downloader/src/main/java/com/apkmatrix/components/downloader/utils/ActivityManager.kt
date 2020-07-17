package com.apkmatrix.components.downloader.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * @author Xiong Ke
 * @date 2017/12/6
 */
class ActivityManager private constructor() {
    private val lifecycleStackActivity by lazy { Stack<Activity>() }
    private val baseActivities by lazy { HashSet<Activity>() }
    private val myActivityLifecycleCallbacks by lazy { MyActivityLifecycleCallbacks() }
    private var isRegister = false

    companion object {
        private var activityManager: ActivityManager? = null
        private var mApplication: Application? = null

        val instance: ActivityManager
            get() {
                if (activityManager == null) {
                    synchronized(ActivityManager::class.java) {
                        if (activityManager == null) {
                            activityManager = ActivityManager()
                        }
                    }
                }
                return activityManager!!
            }

        fun initial(mApplication: Application) {
            this.mApplication = mApplication
            instance.register()
        }
    }

    val stackActivityCount: Int
        get() = lifecycleStackActivity.size

    val stackTopActiveActivity: Activity?
        get() = if (lifecycleStackActivity.isEmpty()) {
            null
        } else {
            lifecycleStackActivity.lastElement()
        }

    private inner class MyActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity?) = Unit
        override fun onActivityPaused(activity: Activity?) = Unit
        override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) = Unit
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            activity?.let {
                baseActivities.add(it)
            }
        }

        override fun onActivityDestroyed(activity: Activity?) {
            activity?.let {
                baseActivities.remove(it)
            }
        }

        override fun onActivityStarted(activity: Activity?) {
            activity?.let {
                lifecycleStackActivity.add(it)
            }
        }

        override fun onActivityStopped(activity: Activity?) {
            activity?.let {
                lifecycleStackActivity.remove(it)
            }
        }
    }

    private fun register() {
        if (!isRegister) {
            isRegister = true
            mApplication?.registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks)
        }
    }

    private fun unregister() {
        if (isRegister) {
            isRegister = false
            mApplication?.unregisterActivityLifecycleCallbacks(myActivityLifecycleCallbacks)
        }
    }
}
