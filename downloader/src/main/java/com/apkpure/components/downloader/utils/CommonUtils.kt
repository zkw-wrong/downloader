package com.apkpure.components.downloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apkpure.components.downloader.R
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import com.apkpure.components.downloader.db.enums.DownloadTaskStatusType
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

    fun downloadStateNotificationInfo(mContext: Context, missionDbBean: DownloadTaskBean): String {
        return when (missionDbBean.downloadTaskStatusType) {
            DownloadTaskStatusType.Waiting -> mContext.getString(R.string.q_waiting)
            DownloadTaskStatusType.Preparing -> mContext.getString(R.string.q_preparing)
            DownloadTaskStatusType.Downloading -> mContext.getString(R.string.q_downloading)
            DownloadTaskStatusType.Success -> mContext.getString(R.string.q_download_complete)
            DownloadTaskStatusType.Stop -> mContext.getString(R.string.q_paused)
            DownloadTaskStatusType.Failed -> mContext.getString(R.string.q_failed)
            else -> String()
        }
    }

    fun startService(mContext: Context, intent: Intent) {
        mContext.startService(intent)
    }

    private fun startForegroundService(mContext: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent)
        } else {
            mContext.startService(intent)
        }
    }
}