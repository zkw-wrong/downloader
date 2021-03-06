package com.apkmatrix.components.downloader.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.apkmatrix.components.downloader.R
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.misc.TaskConfig
import kotlinx.coroutines.*

/**
 * @author xiongke
 * @date 2019/2/21
 */
internal class NotifyHelper(private val mService: Service) {
    private val mContext1: Context = mService
    private val notificationChannelId by lazy { "Notification-Id" }
    private val notificationChannelName by lazy { "Notification-Name" }
    private val notificationManager by lazy { mContext1.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private var downloadIngNotification: NotificationCompat.Builder? = null
    private var downloadCompatNotification: NotificationCompat.Builder? = null
    private var downloadFailedNotification: NotificationCompat.Builder? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(notificationChannelId, notificationChannelName,
                    NotificationManager.IMPORTANCE_LOW).apply {
                this.setShowBadge(true)
                this.enableLights(true)
                this.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                this.setBypassDnd(true)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    companion object {
        private const val timeMillisForeground = 250L
        var foregroundNotifyId = 0
    }

    fun init() {
        foregroundNotifyId = 0
        mService.stopForeground(true)
        notificationManager.cancel(foregroundNotifyId)
    }

    @SuppressLint("RestrictedApi")
    fun hintTaskIngNotify(downloadTask: DownloadTask) {
        downloadIngNotification = downloadIngNotification
                ?: NotificationCompat.Builder(mContext1, notificationChannelId)
                        .setSmallIcon(R.drawable.download_status_downloading)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setShowWhen(false)
        downloadIngNotification?.apply {
            if (downloadTask.notificationTitle.isNotEmpty()) {
                this.setContentTitle(downloadTask.notificationTitle)
            }
            TaskConfig.notificationLargeIcon?.let {
                this.setLargeIcon(it)
            }
            downloadTask.notificationIntent?.let {
                this.setContentIntent(getNotificationContentIntent(it))
            }
            this.setContentText(CommonUtils.downloadStateNotificationInfo(mContext1, downloadTask))
            val progress = CommonUtils.formatPercent(downloadTask.currentOffset, downloadTask.totalLength)
            this.setProgress(100, progress, false)
            val build = this.build()
            if (foregroundNotifyId == 0 && !CommonUtils.isServiceForegroundRunning(mContext)) {
                foregroundNotifyId = downloadTask.notificationId
                mService.startForeground(foregroundNotifyId, build)
            } else {
                notificationManager.notify(downloadTask.notificationId, build)
            }
        }
    }

    fun hintDownloadCompleteNotify(downloadTask: DownloadTask) {
        downloadCompatNotification = downloadCompatNotification
                ?: NotificationCompat.Builder(mContext1, notificationChannelId)
                        .setSmallIcon(R.drawable.download_status_success)
                        .setContentTitle(mContext1.getString(R.string.q_download_complete))
                        .setOngoing(false)
                        .setAutoCancel(true)
        downloadCompatNotification?.apply {
            if (downloadTask.notificationTitle.isNotEmpty()) {
                this.setContentTitle(downloadTask.notificationTitle)
            }
            TaskConfig.notificationLargeIcon?.let {
                this.setLargeIcon(it)
            }
            downloadTask.notificationIntent?.let {
                this.setContentIntent(getNotificationContentIntent(it))
            }
            this.setContentText(CommonUtils.downloadStateNotificationInfo(mContext1, downloadTask))
            val build = this.build()
            GlobalScope.launch {
                if (downloadTask.notificationId == foregroundNotifyId) {
                    mService.stopForeground(true)
                    foregroundNotifyId = 0
                    withContext(Dispatchers.IO) {
                        delay(timeMillisForeground)
                    }
                }
                notificationManager.cancel(downloadTask.notificationId)
                notificationManager.notify(downloadTask.notificationId, build)
            }
        }
    }

    fun hintDownloadFailed(downloadTask: DownloadTask) {
        downloadFailedNotification = downloadFailedNotification
                ?: NotificationCompat.Builder(mContext1, notificationChannelId)
                        .setSmallIcon(R.drawable.download_status_failed)
                        .setOngoing(false)
                        .setAutoCancel(true)
        downloadFailedNotification?.apply {
            if (downloadTask.notificationTitle.isNotEmpty()) {
                this.setContentTitle(downloadTask.notificationTitle)
            }
            TaskConfig.notificationLargeIcon?.let {
                this.setLargeIcon(it)
            }
            downloadTask.notificationIntent?.let {
                this.setContentIntent(getNotificationContentIntent(it))
            }
            this.setContentText(CommonUtils.downloadStateNotificationInfo(mContext1, downloadTask))
            val build = this.build()
            GlobalScope.launch {
                if (downloadTask.notificationId == foregroundNotifyId) {
                    mService.stopForeground(true)
                    foregroundNotifyId = 0
                    withContext(Dispatchers.IO) {
                        delay(timeMillisForeground)
                    }
                }
                notificationManager.cancel(downloadTask.notificationId)
                notificationManager.notify(downloadTask.notificationId, build)
            }
        }
    }

    fun cancel(notifyId: Int) {
        GlobalScope.launch {
            if (notifyId == foregroundNotifyId) {
                mService.stopForeground(true)
                foregroundNotifyId = 0
                withContext(Dispatchers.IO) {
                    delay(timeMillisForeground)
                }
            }
            notificationManager.cancel(notifyId)
        }
    }

    private fun getNotificationContentIntent(intent: Intent): PendingIntent {
        return PendingIntent.getActivity(mContext1, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun destroy() {
        foregroundNotifyId = 0
        mService.stopForeground(true)
        notificationManager.cancelAll()
    }
}