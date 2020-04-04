package com.apkpure.components.downloader.service

import android.app.Application
import android.content.Context
import com.apkpure.components.downloader.db.DownloadDatabase
import com.apkpure.components.downloader.db.DownloadTaskBean
import com.apkpure.components.downloader.db.Extras
import com.apkpure.components.downloader.service.misc.TaskConfig
import com.apkpure.components.downloader.service.misc.TaskManager
import com.apkpure.components.downloader.service.services.DownloadServiceAssistUtils
import com.apkpure.components.downloader.service.services.DownloadServiceV14
import com.apkpure.components.downloader.utils.CommonUtils
import com.apkpure.components.downloader.utils.PermissionUtils
import okhttp3.OkHttpClient

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
    }

    private fun startInitialTask(mContext: Context) {
        CommonUtils.startService(mContext, DownloadServiceAssistUtils.newInitIntent(mContext
                , DownloadServiceV14::class.java))
    }

    fun getDownloadTasks() = mutableListOf<DownloadTaskBean>().apply {
        addAll(DownloadServiceAssistUtils.downloadTaskLists)
    }

    fun getDownloadTask(taskUrl: String): DownloadTaskBean? {
        var downloadTaskBean: DownloadTaskBean? = null
        DownloadServiceAssistUtils.downloadTaskLists.iterator().forEach {
            if (it.url == taskUrl) {
                downloadTaskBean = it
            }
        }
        return downloadTaskBean
    }

    fun startTask(mContext: Context, url: String,
                  fileName: String? = null, notificationTitle: String? = null,
                  extras: Extras? = null, silent: Boolean = false,
                  showNotification: Boolean = true) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStartIntent(mContext
                    , DownloadServiceV14::class.java, DownloadTaskBean().apply {
                this.url = url
                this.absolutePath = TaskConfig.getOkDownloadAbsolutePath(fileName)
                this.showNotification = showNotification
                this.extras = extras
                this.notificationTitle = notificationTitle
            }))
        }
    }

    fun stopTask(mContext: Context, taskUrl: String, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStopIntent(mContext
                    , DownloadServiceV14::class.java, taskUrl))
        }
    }

    fun deleteTask(mContext: Context, taskUrlList: ArrayList<String>, isDeleteFile: Boolean, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext
                    , DownloadServiceV14::class.java, taskUrlList, isDeleteFile))
        }
    }

    fun deleteAllTask(mContext: Context, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteAllIntent(mContext
                    , DownloadServiceV14::class.java))
        }
    }

    fun renameTaskFile(mContext: Context, taskUrl: String, fileName: String, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newRenameIntent(mContext
                    , DownloadServiceV14::class.java, taskUrl, fileName))
        }
    }
}