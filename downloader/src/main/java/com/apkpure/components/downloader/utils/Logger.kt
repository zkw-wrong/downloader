package com.apkpure.components.downloader.utils

import android.util.Log

/**
 * author: mr.xiong
 * date: 2020/3/23
 */
object Logger {
    var isDebug = false

    fun d(message: String, msg: String) {
        if (isDebug) {
            Log.d(message, msg)
        }
    }
}