package com.apkpure.components.downloader.utils

import android.util.Log
import com.apkpure.components.downloader.service.misc.TaskConfig

/**
 * author: mr.xiong
 * date: 2020/3/23
 */
object Logger {
    fun d(message: String, msg: String) {
        if (TaskConfig.isDebug) {
            Log.d(message, msg)
        }
    }
}