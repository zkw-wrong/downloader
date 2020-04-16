package com.apkpure.components.downloader.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.apkpure.components.downloader.R
import com.apkpure.components.downloader.utils.CommonUtils
import com.apkpure.components.downloader.utils.Logger

/**
 * @author xiongke
 * @date 2019/1/23
 */
class DownloadService14 : Service() {
    private val logTag: String by lazy { javaClass.simpleName }
    private val foregroundId = logTag.hashCode()
    private val downloadServiceAssistUtils by lazy {
        DownloadServiceAssistUtils(mContext, DownloadService14::class.java)
    }
    private val mContext by lazy { this }
    private var foregroundNotification: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()
        Logger.d(logTag, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(logTag, "onStartCommand ${intent?.action}")
        intent?.let {
            downloadServiceAssistUtils.handlerIntent(it)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(logTag, "onDestroy")
    }

    private fun showForegroundNotification() {
        foregroundNotification = foregroundNotification
                ?: NotificationCompat.Builder(mContext, CommonUtils.notificationChannelId)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setShowWhen(false)
                        .setContentTitle(mContext.getString(R.string.q_download_service_notify))
        foregroundNotification?.let {
            startForeground(foregroundId, it.build())
        }
    }

    private fun removeForegroundNotification() {
        stopForeground(true)
    }
}