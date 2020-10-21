package com.apkmatrix.components.downloader.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * @author Xiong Ke
 * @date 2017/12/6
 */
internal class ActivityManager private constructor() {
    private val activeActivityStacks by lazy { Stack<Activity>() }
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
        get() = activeActivityStacks.size

    val stackTopActiveActivity: Activity?
        get() = if (activeActivityStacks.isEmpty()) {
            null
        } else {
            activeActivityStacks.lastElement()
        }

    private inner class MyActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit

        override fun onActivityStarted(activity: Activity) {
            activeActivityStacks.add(activity)
        }

        override fun onActivityStopped(activity: Activity) {
            activeActivityStacks.remove(activity)
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
