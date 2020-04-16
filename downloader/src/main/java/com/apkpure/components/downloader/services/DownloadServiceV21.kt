package com.apkpure.components.downloader.services

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import com.apkpure.components.downloader.utils.Logger

/**
 * @author xiongke
 * @date 2018/11/5
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DownloadServiceV21 : JobIntentService() {
    private val downloadServiceAssistUtils by lazy { DownloadServiceAssistUtils(mContext, DownloadServiceV21::class.java) }
    private val logTag: String by lazy { javaClass.simpleName }
    private val mContext by lazy { this }

    companion object {
        private val serviceId = DownloadServiceV21::javaClass.name.hashCode()

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun enqueueWorkService(mContext: Context, intent: Intent) {
            enqueueWork(mContext, DownloadServiceV21::class.java, serviceId, intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d(logTag, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(logTag, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleWork(intent: Intent) {
        Logger.d(logTag, "onHandleWork ${intent.action}")
        downloadServiceAssistUtils.handlerIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(logTag, "onDestroy")
    }
}