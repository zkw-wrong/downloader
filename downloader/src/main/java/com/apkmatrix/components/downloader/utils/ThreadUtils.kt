package com.apkmatrix.components.downloader.utils

import android.os.Handler
import android.os.Looper

/**
 * @author xiongke
 * @date 2020/6/11
 */
object ThreadUtils {
    private val mainHandler = Handler(Looper.getMainLooper())
    private fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    fun runMainThread(runnable: Runnable) {
        if (isMainThread()) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    fun runThreadPollProxy(runnable: Runnable){
        ThreadManager.threadPollProxy.execute(runnable)
    }
}