package com.apkpure.components.downloader.service.misc

import com.apkpure.components.downloader.db.DownloadTaskBean
import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import com.apkpure.components.downloader.service.DownloadManager
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.SpeedCalculator
import com.liulishuo.okdownload.core.breakpoint.BlockInfo
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend

/**
 * @author xiongke
 * @date 2018/11/16
 */
class CustomDownloadListener4WithSpeed : DownloadListener4WithSpeed() {
    private var taskListener: TaskListener? = null
    private val retryTagKey = 999

    interface TaskListener {
        fun onStart(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatus: DownloadTaskStatus)
        fun onInfoReady(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatus: DownloadTaskStatus, totalLength: Long)
        fun onProgress(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, taskSpeed: String, downloadTaskStatus: DownloadTaskStatus, currentOffset: Long)
        fun onCancel(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatus: DownloadTaskStatus)
        fun onSuccess(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatus: DownloadTaskStatus)
        fun onError(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatus: DownloadTaskStatus)
        fun onRetry(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatus: DownloadTaskStatus, retryCount: Int)
    }

    fun setTaskListener(taskListener: TaskListener) {
        this.taskListener = taskListener
    }

    override fun taskStart(task: DownloadTask) {
        if (task.tag == DownloadTaskActionTag.DELETE) {
            return
        }
        taskListener?.onStart(DownloadManager.instance.getDownloadTask(task.url), task, DownloadTaskStatus.Waiting)
    }

    override fun blockEnd(task: DownloadTask, blockIndex: Int, info: BlockInfo?, blockSpeed: SpeedCalculator) = Unit

    override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, taskSpeed: SpeedCalculator) {
        if (task.tag == DownloadTaskActionTag.DELETE) {
            return
        }
        if (task.tag == DownloadTaskActionTag.PROGRESS_100) {
            return
        }
        val missionDbBean = DownloadManager.instance.getDownloadTask(task.url)
        when (cause) {
            EndCause.COMPLETED -> taskListener?.onSuccess(missionDbBean, task, DownloadTaskStatus.Success)
            EndCause.CANCELED -> taskListener?.onCancel(missionDbBean, task, DownloadTaskStatus.Stop)
            EndCause.FILE_BUSY -> taskListener?.onStart(missionDbBean, task, DownloadTaskStatus.Waiting)
            EndCause.SAME_TASK_BUSY -> taskListener?.onStart(missionDbBean, task, DownloadTaskStatus.Preparing)
            else -> {
                var retryObj = task.getTag(retryTagKey)
                if (retryObj == null) {
                    task.addTag(retryTagKey, 1)
                    task.enqueue(this)
                    taskListener?.onRetry(missionDbBean, task, DownloadTaskStatus.Retry, 1)
                } else if (retryObj is Int) {
                    retryObj += 1
                    task.addTag(retryTagKey, retryObj)
                    if (retryObj >= TaskConfig.failedRetryCount) {
                        taskListener?.onError(missionDbBean, task, DownloadTaskStatus.Failed)
                    } else {
                        task.enqueue(this)
                        taskListener?.onRetry(missionDbBean, task, DownloadTaskStatus.Retry, retryObj)
                    }
                }
            }
        }
    }

    override fun progress(task: DownloadTask, currentOffset: Long, taskSpeed: SpeedCalculator) {
        if (task.tag == DownloadTaskActionTag.DELETE) {
            return
        }
        if (task.tag == DownloadTaskActionTag.PAUSED) {
            return
        }
        val missionDbBean = DownloadManager.instance.getDownloadTask(task.url)
        if (currentOffset == missionDbBean?.totalLength) {//防止OkDownload Progress到100%不走taskEnd
            task.tag = DownloadTaskActionTag.PROGRESS_100
            taskListener?.onSuccess(missionDbBean, task, DownloadTaskStatus.Success)
        } else {
            taskListener?.onProgress(missionDbBean, task, taskSpeed.speed(), DownloadTaskStatus.Downloading, currentOffset)
        }
    }

    override fun connectEnd(task: DownloadTask, blockIndex: Int, responseCode: Int, responseHeaderFields: MutableMap<String, MutableList<String>>) = Unit

    override fun connectStart(task: DownloadTask, blockIndex: Int, requestHeaderFields: MutableMap<String, MutableList<String>>) = Unit

    override fun infoReady(task: DownloadTask, info: BreakpointInfo, fromBreakpoint: Boolean, model: Listener4SpeedAssistExtend.Listener4SpeedModel) {
        if (task.tag == DownloadTaskActionTag.DELETE) {
            return
        }
        taskListener?.onInfoReady(DownloadManager.instance.getDownloadTask(task.url), task, DownloadTaskStatus.Preparing, info.totalLength)
    }

    override fun progressBlock(task: DownloadTask, blockIndex: Int, currentBlockOffset: Long, blockSpeed: SpeedCalculator) = Unit
}