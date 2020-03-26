package com.apkpure.components.downloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.text.DecimalFormat

/**
 * author: mr.xiong
 * date: 2020/3/26
 */
object CommonUtils {
    val notificationChannelId by lazy { "Notification-Id" }
    val notificationChannelName by lazy { "Notification-Name" }

    fun register(mContext: Context?, receiver: BroadcastReceiver?, vararg actions: String?) {
        val filter = IntentFilter()
        for (action in actions) {
            filter.addAction(action)
        }
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(receiver!!, filter)
    }

    fun unregister(mContext: Context?, receiver: BroadcastReceiver?) {
        LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(receiver!!)
    }

    fun formatFileLength(sizeBytes: Long): String {
        if (sizeBytes <= 0) {
            return "0B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "EB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#.##").format(sizeBytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun formatPercent(progress: Long, count: Long): Int {
        return (progress * 1f / count * 100f).toInt()
    }

    fun formatPercentInfo(progress: Long, count: Long): String {
        return DecimalFormat("##%").format(progress.toDouble() / count.toDouble())
    }
}