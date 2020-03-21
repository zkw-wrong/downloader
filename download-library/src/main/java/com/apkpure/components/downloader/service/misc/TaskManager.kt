package com.xk.gvido.app.ui.service.misc

import com.apkpure.components.downloader.service.misc.DownloadListener
import com.liulishuo.okdownload.DownloadContext
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import com.xk.gvido.app.app.App
import com.xk.gvido.app.model.net.api.manager.ApiManager
import java.io.File

/**
 * @author xiongke
 * @date 2018/11/19
 */
class TaskManager {
    private var downloadListener: DownloadListener? = null
    private lateinit var downloadBuilder: DownloadContext.Builder

    companion object {
        private var taskManager: TaskManager? = null
        fun getInstance(): TaskManager {
            if (taskManager == null) {
                synchronized(TaskManager::class.java) {
                    if (taskManager == null) {
                        taskManager = TaskManager()
                                .apply {
                                    initial()
                                }
                    }
                }
            }
            return taskManager!!
        }
    }

    private fun initial() {
        OkDownload.setSingletonInstance(OkDownload.Builder(App.mContext)
                .connectionFactory(DownloadOkHttp3Connection
                        .Factory()
                        .setBuilder(ApiManager.instance.newOkHttpClientBuilder(false)))
                .build())
        DownloadDispatcher.setMaxParallelRunningCount(TaskConfig.maxRunningCount)
        DownloadContext.QueueSet().apply {
            this.minIntervalMillisCallbackProcess = TaskConfig.minIntervalMillisCallbackProcess
            this.isWifiRequired = TaskConfig.isWifiRequired
            this.isAutoCallbackToUIThread = TaskConfig.isAutoCallbackToUIThread
            downloadBuilder = this.commit()
        }
    }

    private fun isExistsTask(taskUrl: String): Boolean {
        var flag = false
        downloadBuilder.build().tasks.iterator().forEach {
            flag = it.url == taskUrl
        }
        return flag
    }

    private fun getTask(taskUrl: String): DownloadTask? {
        var downloadTask: DownloadTask? = null
        this.downloadBuilder.build().tasks.iterator().forEach {
            if (it.url == taskUrl) {
                downloadTask = it
            }
        }
        return downloadTask
    }

    fun setDownloadListener(downloadListener: DownloadListener) {
        this.downloadListener = downloadListener
    }

    fun start(downloadUrl: String, absolutePath: String) {
        if (isExistsTask(downloadUrl)) {
            getTask(downloadUrl)?.apply {
                downloadBuilder.bindSetTask(this)
                this.tag = TaskActionTag.Default
                this.enqueue(downloadListener)
            }
        } else {
            downloadBuilder
                    .bind(DownloadTask.Builder(downloadUrl, File(absolutePath)))
                    .apply {
                        this.tag = TaskActionTag.Default
                        this.enqueue(downloadListener)
                    }
        }
    }

    fun startOnParallel() {
        this.downloadBuilder.build().apply {
            this.tasks.iterator().forEach {
                it.tag = TaskActionTag.Default
            }
            this.startOnParallel(downloadListener)
        }
    }

    fun stop(downloadUrl: String) {
        getTask(downloadUrl)?.apply {
            this.tag = TaskActionTag.PAUSED
            this.cancel()
        }
    }

    fun delete(downloadUrl: String) {
        getTask(downloadUrl)?.apply {
            this.tag = TaskActionTag.DELETE
            this.cancel()
        }
    }

    fun deleteAll() {
        val downloadContext = downloadBuilder.build().apply {
            this.tasks.iterator().forEach {
                it.tag = TaskActionTag.DELETE
            }
        }
        if (downloadContext.isStarted) {
            downloadContext.stop()
        }
    }

    fun stopAll() {
        val downloadContext = downloadBuilder.build().apply {
            this.tasks.iterator().forEach {
                it.tag = TaskActionTag.PAUSED
            }
        }
        if (downloadContext.isStarted) {
            downloadContext.stop()
        }
    }
}