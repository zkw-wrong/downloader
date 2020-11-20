package com.apkmatrix.components.downloader.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.apkmatrix.components.downloader.utils.Logger

/**
 * @author xiongke
 * @date 2019/1/23
 */
class DownloadService : Service() {
    private val logTag: String by lazy { javaClass.simpleName }
    private var downloadServiceAssistUtils: DownloadServiceAssistUtils? = null

    override fun onCreate() {
        super.onCreate()
        Logger.d(logTag, "onCreate")
        getDownloadServiceAssistUtils()?.initialService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            getDownloadServiceAssistUtils()?.handlerIntent(it)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        getDownloadServiceAssistUtils()?.destroy()
        super.onDestroy()
        Logger.d(logTag, "onDestroy")
    }

    private fun getDownloadServiceAssistUtils(): DownloadServiceAssistUtils? {
        if (downloadServiceAssistUtils == null) {
            downloadServiceAssistUtils = DownloadServiceAssistUtils(this)
        }
        return downloadServiceAssistUtils
    }
}