package com.apkpure.components.downloader.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * @author xiongke
 * @date 2019/2/21
 */
class NotifyHelper(val mContext: Context) {
    private val notificationChannelId by lazy { "Notification-Id" }
    private val notificationChannelName by lazy { "Notification-Name" }
    val notificationManager by lazy { mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    // val notificationIcon by lazy { AppUtils.getAppIcon(mContext.packageManager,mContext.applicationInfo) }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                this.setShowBadge(true)
                this.enableLights(true)
                this.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                this.setBypassDnd(true)
                notificationManager.createNotificationChannel(this)
            }
        }
    }
}