package com.apkpure.components.downloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apkpure.components.downloader.R
import com.apkpure.components.downloader.db.DownloadTask
import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import java.io.File
import java.text.DecimalFormat
import kotlin.math.roundToInt

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

    fun randomNumber(min: Int, max: Int): Int {
        return (Math.random() * (max - min) + min).roundToInt()
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

    fun downloadStateNotificationInfo(mContext: Context, downloadTask: DownloadTask): String {
        return when (downloadTask.downloadTaskStatus) {
            DownloadTaskStatus.Waiting -> mContext.getString(R.string.q_waiting)
            DownloadTaskStatus.Preparing -> mContext.getString(R.string.q_preparing)
            DownloadTaskStatus.Downloading -> mContext.getString(R.string.q_downloading)
            DownloadTaskStatus.Success -> mContext.getString(R.string.q_download_complete)
            DownloadTaskStatus.Stop -> mContext.getString(R.string.q_paused)
            DownloadTaskStatus.Failed -> mContext.getString(R.string.q_failed)
            else -> String()
        }
    }

    fun replaceLast(text: String, regex: String, replacement: String): String {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + "\\)".toRegex(), replacement)
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

    fun createAvailableFileName(defaultFile: File): File {
        var index = 1
        val fileDir = defaultFile.parentFile
        var fileName = defaultFile.name
        return if (FsUtils.exists(defaultFile)) {
            index++
            fileName = if (fileName.contains(".")) {
                replaceLast(fileName, ".", ".($index)")
            } else {
                "$fileName($index)"
            }
            createAvailableFileName(File(fileDir, fileName))
        } else {
            defaultFile
        }
    }
}