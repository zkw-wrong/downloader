package com.apkmatrix.components.downloader.misc

import android.content.Context
import androidx.annotation.IntRange
import com.apkmatrix.components.downloader.DownloadManager
import com.apkmatrix.components.downloader.db.DownloadTask
import com.liulishuo.okdownload.DownloadContext
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import okhttp3.OkHttpClient
import java.io.File
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * @author xiongke
 * @date 2018/11/19
 */
internal class TaskManager {
    private var customDownloadListener4WithSpeed: CustomDownloadListener4WithSpeed? = null
    private lateinit var downloadBuilder: DownloadContext.Builder
    private var okDownload: OkDownload? = null

    companion object {
        const val retryTagKey = 999
        const val taskIdTagKey = 998
        private var taskManager: TaskManager? = null
        val instance: TaskManager
            get() {
                if (taskManager == null) {
                    synchronized(TaskManager::class.java) {
                        if (taskManager == null) {
                            taskManager = TaskManager()
                        }
                    }
                }
                return taskManager!!
            }

        fun init(mContext: Context, builder: OkHttpClient.Builder) {
            instance.initial(mContext, builder)
        }
    }

    private fun initial(mContext: Context, builder: OkHttpClient.Builder) {
        if (okDownload == null) {
            synchronized(TaskManager::class.java) {
                if (okDownload == null) {
                    okDownload = OkDownload.Builder(mContext)
                            .connectionFactory(DownloadOkHttp3Connection.Factory().setBuilder(builder))
                            .build()
                    OkDownload.setSingletonInstance(okDownload!!)
                }
            }
        }
        setMaxParallelRunningCount()
        DownloadContext.QueueSet().apply {
            this.minIntervalMillisCallbackProcess = TaskConfig.minIntervalMillisCallbackProcess
            this.isWifiRequired = false
            this.isAutoCallbackToUIThread = true
            this.isPassIfAlreadyCompleted = false
            downloadBuilder = this.commit()
        }
    }

    fun setMaxParallelRunningCount(@IntRange(from = 1) maxRunningCount: Int = 5) {
        DownloadDispatcher.setMaxParallelRunningCount(maxRunningCount)
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
            val task = taskBuilder.build().apply {
                this.tag = DownloadTaskActionTag.Default
                this.addTag(taskIdTagKey, taskId)
                this.enqueue(customDownloadListener4WithSpeed)
            }
            downloadBuilder.bindSetTask(task)
            OkDownload.with().callbackDispatcher().dispatch().taskEnd(task,
                    EndCause.SAME_TASK_BUSY, null)
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
        DownloadManager.getDownloadTask(taskId)?.let {
            start(it)
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