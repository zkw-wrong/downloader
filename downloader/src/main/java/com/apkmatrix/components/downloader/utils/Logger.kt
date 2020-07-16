package com.apkmatrix.components.downloader.utils

import android.util.Log
import com.apkmatrix.components.downloader.misc.TaskConfig

/**
 * author: mr.xiong
 * date: 2020/3/23
 */
object Logger {
    fun d(tag: String, msg: String) {
        if (TaskConfig.isDebug) {
            Log.d(tag, msg)
        }
    }
}