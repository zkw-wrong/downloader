package com.apkpure.components.downloader.service.misc

import com.apkpure.components.downloader.db.bean.MissionDbBean
import com.apkpure.components.downloader.db.enums.MissionStatusType
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
        fun onStart(missionDbBean: MissionDbBean?, task: DownloadTask, missionStatusType: MissionStatusType)
        fun onInfoReady(missionDbBean: MissionDbBean?, task: DownloadTask, missionStatusType: MissionStatusType, totalLength: Long)
        fun onProgress(missionDbBean: MissionDbBean?, task: DownloadTask, taskSpeed: String, missionStatusType: MissionStatusType, currentOffset: Long)
        fun onCancel(missionDbBean: MissionDbBean?, task: DownloadTask, missionStatusType: MissionStatusType)
        fun onSuccess(missionDbBean: MissionDbBean?, task: DownloadTask, missionStatusType: MissionStatusType)
        fun onError(missionDbBean: MissionDbBean?, task: DownloadTask, missionStatusType: MissionStatusType)
        fun onRetry(missionDbBean: MissionDbBean?, task: DownloadTask, missionStatusType: MissionStatusType, retryCount: Int)
    }

    fun setTaskListener(taskListener: TaskListener) {
        this.taskListener = taskListener
    }

    override fun taskStart(task: DownloadTask) {
        if (task.tag == DownloadTaskActionTag.DELETE) {
            return
        }
        taskListener?.onStart(DownloadManager.instance.getMissionTask(task.url), task, MissionStatusType.Waiting)
    }

    override fun blockEnd(task: DownloadTask, blockIndex: Int, info: BlockInfo?, blockSpeed: SpeedCalculator) = Unit

    override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, taskSpeed: SpeedCalculator) {
        if (task.tag == DownloadTaskActionTag.DELETE) {
            return
        }
        if (task.tag == DownloadTaskActionTag.PROGRESS_100) {
            return
        }
        val missionDbBean = DownloadManager.instance.getMissionTask(task.url)
        when (cause) {
            EndCause.COMPLETED -> taskListener?.onSuccess(missionDbBean, task, MissionStatusType.Success)
            EndCause.CANCELED -> taskListener?.onCancel(missionDbBean, task, MissionStatusType.Stop)
            EndCause.FILE_BUSY -> taskListener?.onStart(missionDbBean, task, MissionStatusType.Waiting)
            EndCause.SAME_TASK_BUSY -> taskListener?.onStart(missionDbBean, task, MissionStatusType.Preparing)
            else -> {
                var retryObj = task.getTag(retryTagKey)
                if (retryObj == null) {
                    task.addTag(retryTagKey, 1)
                    task.enqueue(this)
                    taskListener?.onRetry(missionDbBean, task, MissionStatusType.Retry, 1)
                } else if (retryObj is Int) {
                    retryObj += 1
                    task.addTag(retryTagKey, retryObj)
                    if (retryObj >= DownloadTaskConfig.failedRetryCount) {
                        taskListener?.onError(missionDbBean, task, MissionStatusType.Failed)
                    } else {
                        task.enqueue(this)
                        taskListener?.onRetry(missionDbBean, task, MissionStatusType.Retry, retryObj)
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
        val missionDbBean = DownloadManager.instance.getMissionTask(task.url)
        if (currentOffset == missionDbBean?.totalLength) {//防止OkDownload Progress到100%不走taskEnd
            task.tag = DownloadTaskActionTag.PROGRESS_100
            taskListener?.onSuccess(missionDbBean, task, MissionStatusType.Success)
        } else {
            taskListener?.onProgress(missionDbBean, task, taskSpeed.speed(), MissionStatusType.Downloading, currentOffset)
        }
    }

    override fun connectEnd(task: DownloadTask, blockIndex: Int, responseCode: Int, responseHeaderFields: MutableMap<String, MutableList<String>>) = Unit

    override fun connectStart(task: DownloadTask, blockIndex: Int, requestHeaderFields: MutableMap<String, MutableList<String>>) = Unit

    override fun infoReady(task: DownloadTask, info: BreakpointInfo, fromBreakpoint: Boolean, model: Listener4SpeedAssistExtend.Listener4SpeedModel) {
        if (task.tag == DownloadTaskActionTag.DELETE) {
            return
        }
        taskListener?.onInfoReady(DownloadManager.instance.getMissionTask(task.url), task, MissionStatusType.Preparing, info.totalLength)
    }

    override fun progressBlock(task: DownloadTask, blockIndex: Int, currentBlockOffset: Long, blockSpeed: SpeedCalculator) = Unit
}