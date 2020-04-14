package com.apkpure.components.downloader.service

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import com.apkpure.components.downloader.db.DownloadDatabase
import com.apkpure.components.downloader.db.DownloadTask
import com.apkpure.components.downloader.misc.TaskConfig
import com.apkpure.components.downloader.misc.TaskManager
import com.apkpure.components.downloader.services.DownloadServiceAssistUtils
import com.apkpure.components.downloader.services.DownloadService
import com.apkpure.components.downloader.utils.CommonUtils
import com.apkpure.components.downloader.utils.PermissionUtils
import com.liulishuo.okdownload.core.Util
import okhttp3.OkHttpClient
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * author: mr.xiong
 * date: 2020/3/26
 */
class DownloadManager {
    companion object {
        private var downloadManager: DownloadManager? = null
        private lateinit var application: Application

        fun initial(application: Application, builder: OkHttpClient.Builder) {
            this.application = application
            DownloadDatabase.initial(application)
            TaskManager.init(application, builder)
            instance.startInitialTask(application)
        }

        val instance: DownloadManager
            get() {
                if (downloadManager == null) {
                    synchronized(DownloadManager::class.java) {
                        if (downloadManager == null) {
                            downloadManager = DownloadManager()
                        }
                    }
                }
                return downloadManager!!
            }

        fun setDebug(isDebug: Boolean) {
            TaskConfig.isDebug = isDebug
            if (isDebug){
                Util.enableConsoleLog()
            }
        }

        fun setNotificationLargeIcon(bitmap: Bitmap) {
            TaskConfig.setNotificationLargeIcon(bitmap)
        }
    }

    private fun startInitialTask(mContext: Context) {
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newInitIntent(mContext
                , DownloadService::class.java))
    }

    fun getDownloadTasks() = mutableListOf<DownloadTask>().apply {
        addAll(DownloadServiceAssistUtils.downloadTaskLists)
    }

    fun getDownloadTask(taskId: String): DownloadTask? {
        DownloadServiceAssistUtils.downloadTaskLists.iterator().forEach {
            if (it.id == taskId) {
                return it
            }
        }
        return null
    }

    fun getDownloadTask(okDownloadTask: OkDownloadTask): DownloadTask? {
        TaskManager.instance.getOkDownloadTaskId(okDownloadTask)?.let { it1 ->
            DownloadServiceAssistUtils.downloadTaskLists.iterator().forEach { it2 ->
                if (it1 == it2.id) {
                    return it2
                }
            }
        }
        return null
    }

    fun startNewTask(mContext: Context, builder: DownloadTask.Builder, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStartNewTaskIntent(mContext
                    , DownloadService::class.java, builder.build()))
        }
    }

    fun stopTask(mContext: Context, id: String, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStopIntent(mContext
                    , DownloadService::class.java, id))
        }
    }

    fun resumeTask(mContext: Context, id: String, silent: Boolean= false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newResumeIntent(mContext
                    , DownloadService::class.java, id))
        }
    }

    fun deleteTask(mContext: Context, ids: ArrayList<String>, isDeleteFile: Boolean, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext
                    , DownloadService::class.java, ids, isDeleteFile))
        }
    }

    fun deleteAllTask(mContext: Context, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteAllIntent(mContext
                    , DownloadService::class.java))
        }
    }

    fun renameTaskFile(mContext: Context, id: String, fileName: String, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newRenameIntent(mContext
                    , DownloadService::class.java, id, fileName))
        }
    }
}