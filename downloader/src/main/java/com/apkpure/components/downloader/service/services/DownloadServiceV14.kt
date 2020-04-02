package com.apkpure.components.downloader.service.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.apkpure.components.downloader.utils.AppLogger

/**
 * @author xiongke
 * @date 2019/1/23
 */
class DownloadServiceV14 : Service() {
    private val logTag: String by lazy { javaClass.simpleName }
    private val downloadServiceAssistUtils by lazy {
        DownloadServiceAssistUtils(
            mContext,
            DownloadServiceV14::class.java
        )
    }
    private val mContext by lazy { this }
    override fun onCreate() {
        super.onCreate()
        AppLogger.d(logTag, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.d(logTag, "onStartCommand ${intent?.action}")
        intent?.let {
            downloadServiceAssistUtils.handlerIntent(it)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d(logTag, "onDestroy")
    }

}