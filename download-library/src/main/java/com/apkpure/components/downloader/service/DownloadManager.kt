package com.apkpure.components.downloader.service

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import com.apkpure.components.downloader.db.AppDbHelper
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import com.apkpure.components.downloader.service.misc.TaskManager
import com.apkpure.components.downloader.service.services.DownloadServiceAssistUtils
import com.apkpure.components.downloader.service.services.DownloadServiceV14
import com.apkpure.components.downloader.service.services.DownloadServiceV21
import com.apkpure.components.downloader.service.services.KeepAliveJobService
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
            AppDbHelper.init(application)
            TaskManager.init(application, builder)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                KeepAliveJobService.startJob(application)
            }
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

    private fun startInitialTask(mContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newInitIntent(mContext, DownloadServiceV21::class.java).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newInitIntent(mContext, DownloadServiceV14::class.java))
        }
    }


    fun startClickTask(mContext: Context, downloadTaskBean: DownloadTaskBean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newStartIntent(mContext, DownloadServiceV21::class.java, downloadTaskBean).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newStartIntent(mContext,
                    DownloadServiceV14::class.java, downloadTaskBean))
        }
    }

    fun stopTask(mContext: Context, taskUrl: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newStopIntent(mContext, DownloadServiceV21::class.java, taskUrl).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newStopIntent(mContext,
                    DownloadServiceV14::class.java, taskUrl))
        }
    }

    fun deleteTask(mContext: Context, taskUrl: String, isDeleteFile: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newDeleteIntent(mContext, DownloadServiceV21::class.java, taskUrl, isDeleteFile).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext,
                    DownloadServiceV14::class.java, taskUrl, isDeleteFile))
        }
    }

    fun deleteAllTask(mContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newDeleteAllIntent(mContext, DownloadServiceV21::class.java).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newDeleteAllIntent(mContext,
                    DownloadServiceV14::class.java))
        }
    }

    private fun startService(mContext: Context, intent: Intent) {
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