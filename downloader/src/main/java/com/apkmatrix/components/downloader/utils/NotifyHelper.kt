package com.apkmatrix.components.downloader.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.apkmatrix.components.downloader.R
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.misc.TaskConfig

/**
 * @author xiongke
 * @date 2019/2/21
 */
internal class NotifyHelper(private val mService: Service) {
    private val mContext1: Context = mService
    private var foregroundNotifyId = 0
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
            this.setProgress(downloadTask.totalLength.toInt(), downloadTask.currentOffset.toInt(), false)
            if (!CommonUtils.isServiceForegroundRunning(mContext)) {
                foregroundNotifyId = downloadTask.notificationId
                mService.startForeground(foregroundNotifyId, this.build())
            } else {
                notificationManager.notify(downloadTask.notificationId, this.build())
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
            if (downloadTask.notificationId == foregroundNotifyId) {
                mService.stopForeground(true)
                foregroundNotifyId = 0
            }
            notificationManager.cancel(downloadTask.notificationId)
            notificationManager.notify(downloadTask.notificationId, this.build())
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
            if (downloadTask.notificationId == foregroundNotifyId) {
                mService.stopForeground(true)
                foregroundNotifyId = 0
            }
            notificationManager.cancel(downloadTask.notificationId)
            notificationManager.notify(downloadTask.notificationId, this.build())
        }
    }

    fun cancel(notifyId: Int) {
        if (notifyId == foregroundNotifyId) {
            mService.stopForeground(true)
            foregroundNotifyId = 0
        }
        notificationManager.cancel(notifyId)
    }

    private fun getNotificationContentIntent(intent: Intent): PendingIntent {
        return PendingIntent.getActivity(mContext1, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}