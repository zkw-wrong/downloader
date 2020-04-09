package com.apkpure.components.downloader.service.misc

import android.content.Context
import com.apkpure.components.downloader.db.DownloadTask
import com.liulishuo.okdownload.DownloadContext
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import okhttp3.OkHttpClient
import java.io.File
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * @author xiongke
 * @date 2018/11/19
 */
class TaskManager {
    private var customDownloadListener4WithSpeed: CustomDownloadListener4WithSpeed? = null
    private lateinit var downloadBuilder: DownloadContext.Builder

    companion object {
        const val retryTagKey = 999
        const val taskIdTagKey = 998
        private var taskManager: TaskManager? = null
        private var isInitial = false
        val instance: TaskManager
            get() {
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
            instance.initial(mContext, builder)
        }
    }

    fun initial(mContext: Context, builder: OkHttpClient.Builder) {
        OkDownload.setSingletonInstance(
                OkDownload.Builder(mContext)
                        .connectionFactory(DownloadOkHttp3Connection.Factory().setBuilder(builder))
                        .build())
        DownloadDispatcher.setMaxParallelRunningCount(TaskConfig.maxRunningCount)
        DownloadContext.QueueSet().apply {
            this.minIntervalMillisCallbackProcess =
                    TaskConfig.minIntervalMillisCallbackProcess
            this.isWifiRequired = TaskConfig.isWifiRequired
            this.isAutoCallbackToUIThread = TaskConfig.isAutoCallbackToUIThread
            downloadBuilder = this.commit()
        }
    }

    fun getOkDownloadTaskId(okDownloadTask: OkDownloadTask): String? {
        val taskId = okDownloadTask.getTag(taskIdTagKey)
        return if (taskId is String) {
            taskId
        } else {
            null
        }
    }

    private fun isExistsTask(taskId: String): Boolean {
        downloadBuilder.build().tasks.iterator().forEach {
            if (getOkDownloadTaskId(it) == taskId) {
                return true
            }
        }
        return false
    }

    private fun getTask(taskId: String): OkDownloadTask? {
        this.downloadBuilder.build().tasks.iterator().forEach {
            if (getOkDownloadTaskId(it) == taskId) {
                return it
            }
        }
        return null
    }

    fun setDownloadListener(customDownloadListener4WithSpeed: CustomDownloadListener4WithSpeed) {
        this.customDownloadListener4WithSpeed = customDownloadListener4WithSpeed
    }

    fun start(downloadTask: DownloadTask) {
        val downloadUrl = downloadTask.url
        val absolutePath = downloadTask.absolutePath
        val taskId = downloadTask.id
        if (isExistsTask(taskId)) {
            getTask(taskId)?.apply {
                downloadBuilder.bindSetTask(this)
                this.tag = DownloadTaskActionTag.Default
                this.enqueue(customDownloadListener4WithSpeed)
            }
        } else {
            val taskBuilder = OkDownloadTask.Builder(downloadUrl, File(absolutePath))
            downloadTask.headers?.map?.forEach {
                taskBuilder.addHeader(it.key, it.value)
            }
            downloadBuilder.bind(taskBuilder)
                    .apply {
                        this.tag = DownloadTaskActionTag.Default
                        this.addTag(taskIdTagKey, taskId)
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

    fun stop(taskId: String) {
        getTask(taskId)?.apply {
            this.tag = DownloadTaskActionTag.PAUSED
            this.cancel()
        }
    }

    fun resume(taskId: String) {
        getTask(taskId)?.apply {
            downloadBuilder.bindSetTask(this)
            this.tag = DownloadTaskActionTag.Default
            this.enqueue(customDownloadListener4WithSpeed)
        }
    }

    fun delete(taskId: String) {
        getTask(taskId)?.apply {
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