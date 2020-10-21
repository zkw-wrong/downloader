package com.apkmatrix.components.downloader

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.IntRange
import com.apkmatrix.components.appbaseinterface.BaseAppInterface
import com.apkmatrix.components.downloader.db.DownloadDatabase
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.misc.*
import com.apkmatrix.components.downloader.services.DownloadService
import com.apkmatrix.components.downloader.services.DownloadServiceAssistUtils
import com.apkmatrix.components.downloader.utils.ActivityManager
import com.apkmatrix.components.downloader.utils.CommonUtils
import com.apkmatrix.components.downloader.utils.DialogUtils
import com.liulishuo.okdownload.core.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * author: mr.xiong
 * date: 2020/3/26
 */
object DownloadManager {

    var downloadServiceInitCallback: DownloadServiceInitCallback? = null
    var downloadTaskUpdateDataCallback: DownloadTaskUpdateDataCallback? = null

    fun initial(application: Application, builder: OkHttpClient.Builder, downloadServiceInitCallback: DownloadServiceInitCallback? = null) {
        this.downloadServiceInitCallback = downloadServiceInitCallback
        DownloadDatabase.initial(application)
        ActivityManager.initial(application)
        TaskManager.init(application, builder)
        this.startInitialTask(application)
    }

    fun setDebug(isDebug: Boolean) {
        TaskConfig.isDebug = isDebug
        if (isDebug) {
            Util.enableConsoleLog()
        }
    }

    fun isInitDownloadServiceCompat() = DownloadServiceAssistUtils.isInitDownloadServiceCompat

    fun startUpdateDownloadTaskData(mContext: Context, downloadTaskUpdateDataCallback: DownloadTaskUpdateDataCallback?) {
        this.downloadTaskUpdateDataCallback = downloadTaskUpdateDataCallback
        this.startUpdateDownloadTaskData(mContext)
    }

    fun setNotificationLargeIcon(bitmap: Bitmap) {
        TaskConfig.notificationLargeIcon = bitmap
    }

    fun setMaxParallelRunningCount(@IntRange(from = 1) maxRunningCount: Int) {
        TaskManager.instance.setMaxParallelRunningCount(maxRunningCount)
    }

    fun getDownloadTasks() = mutableListOf<DownloadTask>().apply {
        addAll(DownloadDataManager.instance.getAll())
    }

    fun getDownloadTask(taskId: String): DownloadTask? {
        return DownloadDataManager.instance.findDownloadTask(taskId)
    }

    fun getDownloadTask(okDownloadTask: OkDownloadTask): DownloadTask? {
        TaskManager.instance.getOkDownloadTaskId(okDownloadTask)?.let { it1 ->
            DownloadDataManager.instance.getAll().iterator().forEach { it2 ->
                if (it1 == it2.id) {
                    return it2
                }
            }
        }
        return null
    }

    private fun startInitialTask(mContext: Context) {
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newInitIntent(mContext, DownloadService::class.java))
    }

    private fun startUpdateDownloadTaskData(mContext: Context) {
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newUpdateTaskDataIntent(mContext, DownloadService::class.java))
    }

    suspend fun tryRequestStoragePermission(): Boolean {
        val activity = ActivityManager.instance.stackTopActiveActivity
        return if (activity is BaseAppInterface) {
            activity.requestStoragePermission()
        } else {
            false
        }
    }

    suspend fun startNewTask(mContext: Context, builder: DownloadTask.Builder,
                             permissionSilent: Boolean = false,
                             mobileNetworkSilent: Boolean = false) {
        withContext(Dispatchers.Main) {
            if (!DialogUtils.checkSdUsable(mContext, permissionSilent)) {
                return@withContext
            }
            if (!tryRequestStoragePermission()) {
                return@withContext
            }
            if (!DialogUtils.mobileNetworkDialog(mContext, mobileNetworkSilent)) {
                return@withContext
            }
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStartNewTaskIntent(mContext,
                    DownloadService::class.java, builder.build()))
        }
    }

    fun stopTask(mContext: Context, id: String, permissionSilent: Boolean = false) {
        if (!DialogUtils.checkSdUsable(mContext, permissionSilent)) {
            return
        }
        if (!DialogUtils.checkExternalStorageUsable(mContext, permissionSilent)) {
            return
        }
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStopIntent(mContext, DownloadService::class.java, id))
    }

    fun resumeTask(mContext: Context, id: String, permissionSilent: Boolean = false) {
        if (!DialogUtils.checkSdUsable(mContext, permissionSilent)) {
            return
        }
        if (!DialogUtils.checkExternalStorageUsable(mContext, permissionSilent)) {
            return
        }
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newResumeIntent(mContext, DownloadService::class.java, id))
    }

    fun deleteTask(mContext: Context, id: String, isDeleteFile: Boolean = true, permissionSilent: Boolean = false) {
        if (!DialogUtils.checkSdUsable(mContext, permissionSilent)) {
            return
        }
        if (!DialogUtils.checkExternalStorageUsable(mContext, permissionSilent)) {
            return
        }
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext, DownloadService::class.java, id, isDeleteFile))
    }

    fun deleteAllTask(mContext: Context, permissionSilent: Boolean = false) {
        if (!DialogUtils.checkSdUsable(mContext, permissionSilent)) {
            return
        }
        if (!DialogUtils.checkExternalStorageUsable(mContext, permissionSilent)) {
            return
        }
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteAllIntent(mContext, DownloadService::class.java))
    }

    fun renameTaskFile(mContext: Context, id: String, fileName: String, permissionSilent: Boolean = false) {
        if (!DialogUtils.checkSdUsable(mContext, permissionSilent)) {
            return
        }
        if (!DialogUtils.checkExternalStorageUsable(mContext, permissionSilent)) {
            return
        }
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newRenameIntent(mContext, DownloadService::class.java, id, fileName))
    }
}