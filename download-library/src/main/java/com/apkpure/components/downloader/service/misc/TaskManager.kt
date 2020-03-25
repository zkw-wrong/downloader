package com.apkpure.components.downloader.service.misc

import android.content.Context
import com.liulishuo.okdownload.DownloadContext
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import okhttp3.OkHttpClient
import java.io.File

/**
 * @author xiongke
 * @date 2018/11/19
 */
class TaskManager {
    private var customDownloadListener4WithSpeed: CustomDownloadListener4WithSpeed? = null
    private lateinit var downloadBuilder: DownloadContext.Builder

    companion object {
        private var taskManager: TaskManager? = null
        private var isInitial = false
        fun getInstance(): TaskManager {
            if (taskManager == null) {
                synchronized(TaskManager::class.java) {
                    if (taskManager == null) {
                        taskManager = TaskManager().apply {
                            if (!isInitial) {
                                throw Exception("TaskManager not is initial!")
                            }
                        }
                    }
                }
            }
            return taskManager!!
        }

        fun init(mContext: Context, builder: OkHttpClient.Builder) {
            isInitial = true
            getInstance().initial(mContext, builder)
        }
    }

    fun initial(mContext: Context, builder: OkHttpClient.Builder) {
        OkDownload.setSingletonInstance(
            OkDownload.Builder(mContext)
                .connectionFactory(DownloadOkHttp3Connection.Factory().setBuilder(builder))
                .build()
        )
        DownloadDispatcher.setMaxParallelRunningCount(DownloadTaskConfig.maxRunningCount)
        DownloadContext.QueueSet().apply {
            this.minIntervalMillisCallbackProcess =
                DownloadTaskConfig.minIntervalMillisCallbackProcess
            this.isWifiRequired = DownloadTaskConfig.isWifiRequired
            this.isAutoCallbackToUIThread = DownloadTaskConfig.isAutoCallbackToUIThread
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

    fun setDownloadListener(customDownloadListener4WithSpeed: CustomDownloadListener4WithSpeed) {
        this.customDownloadListener4WithSpeed = customDownloadListener4WithSpeed
    }

    fun start(downloadUrl: String, absolutePath: String) {
        if (isExistsTask(downloadUrl)) {
            getTask(downloadUrl)?.apply {
                downloadBuilder.bindSetTask(this)
                this.tag = DownloadTaskActionTag.Default
                this.enqueue(customDownloadListener4WithSpeed)
            }
        } else {
            downloadBuilder.bind(DownloadTask.Builder(downloadUrl, File(absolutePath)))
                .apply {
                    this.tag = DownloadTaskActionTag.Default
                    this.enqueue(customDownloadListener4WithSpeed)
                }
        }
    }

    fun startOnParallel() {
        this.downloadBuilder.build().apply {
            this.tasks.iterator().forEach {
                it.tag = DownloadTaskActionTag.Default
            }
            this.startOnParallel(customDownloadListener4WithSpeed)
        }
    }

    fun stop(downloadUrl: String) {
        getTask(downloadUrl)?.apply {
            this.tag = DownloadTaskActionTag.PAUSED
            this.cancel()
        }
    }

    fun delete(downloadUrl: String) {
        getTask(downloadUrl)?.apply {
            this.tag = DownloadTaskActionTag.DELETE
            this.cancel()
        }
    }

    fun deleteAll() {
        val downloadContext = downloadBuilder.build().apply {
            this.tasks.iterator().forEach {
                it.tag = DownloadTaskActionTag.DELETE
            }
        }
        if (downloadContext.isStarted) {
            downloadContext.stop()
        }
    }

    fun stopAll() {
        val downloadContext = downloadBuilder.build().apply {
            this.tasks.iterator().forEach {
                it.tag = DownloadTaskActionTag.PAUSED
            }
        }
        if (downloadContext.isStarted) {
            downloadContext.stop()
        }
    }
}