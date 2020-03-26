package com.apkpure.components.downloader.service.services

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import com.apkpure.components.downloader.utils.AppLogger

/**
 * @author xiongke
 * @date 2018/11/5
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DownloadServiceV21 : JobIntentService() {
    private val logTag: String by lazy { javaClass.simpleName }
    private val mContext by lazy { this }
    private val downloadServiceAssistUtils by lazy {
        DownloadServiceAssistUtils(mContext, DownloadServiceV21::class.java)
    }

    companion object {
        private val serviceId = DownloadServiceV21::javaClass.name.hashCode()

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun enqueueWorkService(mContext: Context, intent: Intent) {
            enqueueWork(mContext, DownloadServiceV21::class.java, serviceId, intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppLogger.d(logTag, "onCreate")
        downloadServiceAssistUtils.initial()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.d(logTag, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleWork(intent: Intent) {
        AppLogger.d(logTag, "onHandleWork ${intent.action}")
        downloadServiceAssistUtils.handlerIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d(logTag, "onDestroy")
    }
}