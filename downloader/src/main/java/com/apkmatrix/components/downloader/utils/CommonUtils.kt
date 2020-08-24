package com.apkmatrix.components.downloader.utils

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.CheckResult
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apkmatrix.components.downloader.R
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import com.apkmatrix.components.downloader.services.DownloadService
import java.io.File
import java.text.DecimalFormat
import java.util.ArrayList

/**
 * author: mr.xiong
 * date: 2020/3/26
 */
object CommonUtils {
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
        return DecimalFormat("#.##").format(sizeBytes / Math.pow(1024.0, digitGroups.toDouble())) +
                " " + units[digitGroups]
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

    fun startService(mContext: Context, intent: Intent) {
        try {
            mContext.startService(intent)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun createAvailableFileName(defaultFile: File, index: Int = 1): File {
        var fileName = defaultFile.name
        return if (FsUtils.exists(defaultFile)) {
            val tempIndex = fileName.lastIndexOf(".")
            fileName = if (tempIndex > 0) {
                "${fileName.subSequence(0, tempIndex)}($index)${fileName.substring(tempIndex)}"
            } else {
                "$fileName($index)"
            }
            val tempFile = File(defaultFile.parent, fileName)
            if (FsUtils.exists(tempFile)) {
                createAvailableFileName(defaultFile, index + 1)
            } else {
                tempFile
            }
        } else {
            defaultFile
        }
    }

    @CheckResult
    fun checkSelfPermission(mContext: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    internal fun isServiceForegroundRunning(mContext: Context): Boolean {
        try {
            val downloadServiceClassName = DownloadService::class.java.name
            val myManager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningServiceList = myManager.getRunningServices(99)
                    as ArrayList<ActivityManager.RunningServiceInfo>
            for (i in runningServiceList.indices) {
                val runningService = runningServiceList[i]
                val service = runningService.service
                val className = service.className
                if (className == downloadServiceClassName && runningService.foreground) {
                    return true
                }
            }
        } catch (e: Exception) {
        }
        return false
    }
}