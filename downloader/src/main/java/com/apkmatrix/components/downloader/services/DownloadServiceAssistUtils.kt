package com.apkmatrix.components.downloader.services

import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import com.apkmatrix.components.downloader.DownloadManager
import com.apkmatrix.components.downloader.db.AppDbHelper
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import com.apkmatrix.components.downloader.misc.CustomDownloadListener4WithSpeed
import com.apkmatrix.components.downloader.misc.DownloadTaskChangeLister
import com.apkmatrix.components.downloader.misc.DownloadTaskFileChangeLister
import com.apkmatrix.components.downloader.misc.TaskManager
import com.apkmatrix.components.downloader.utils.CommonUtils
import com.apkmatrix.components.downloader.utils.FsUtils
import com.apkmatrix.components.downloader.utils.Logger
import com.apkmatrix.components.downloader.utils.NotifyHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * @author xiongke
 * @date 2019/1/23
 */
class DownloadServiceAssistUtils(private val mContext1: Context, clazz: Class<*>) {
    private val logTag by lazy { clazz.simpleName }
    private val notifyHelper by lazy { NotifyHelper(mContext1) }
    private val customDownloadListener4WithSpeed by lazy {
        CustomDownloadListener4WithSpeed().apply { this.setTaskListener(getCustomTaskListener()) }
    }

    companion object {
        private const val ServiceFlag = "DownloadService"
        private const val EXTRA_PARAM_ACTION = "download_param_action"
        private const val EXTRA_PARAM_IS_DELETE = "is_delete"
        private const val EXTRA_PARAM_FILE_NAME = "file_name"
        val downloadTaskLists = mutableListOf<DownloadTask>()

        object ActionType {
            const val ACTION_INIT = "$ServiceFlag init"
            const val ACTION_UPDATE_TASK_DATA = "$ServiceFlag update_task_data"
            const val ACTION_NEW_START = "$ServiceFlag new_start"
            const val ACTION_STOP = "$ServiceFlag stop"
            const val ACTION_RESUME = "$ServiceFlag resume"
            const val ACTION_DELETE = "$ServiceFlag delete"
            const val ACTION_START_ALL = "$ServiceFlag start_all"
            const val ACTION_STOP_ALL = "$ServiceFlag stop_all"
            const val ACTION_DELETE_ALL = "$ServiceFlag delete_all"
            const val ACTION_FILE_RENAME = "$ServiceFlag file_rename"
        }

        fun newInitIntent(mContext: Context, clazz: Class<*>): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_INIT
            }
        }

        fun newUpdateTaskDataIntent(mContext: Context, clazz: Class<*>): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_UPDATE_TASK_DATA
            }
        }

        fun newStartNewTaskIntent(mContext: Context, clazz: Class<*>, downloadTask: DownloadTask): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_NEW_START
                this.putExtra(EXTRA_PARAM_ACTION, downloadTask)
            }
        }

        fun newStopIntent(mContext: Context, clazz: Class<*>, taskId: String): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_STOP
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
            }
        }

        fun newResumeIntent(mContext: Context, clazz: Class<*>, taskId: String): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_RESUME
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
            }
        }

        fun newDeleteIntent(mContext: Context, clazz: Class<*>, taskIds: ArrayList<String>, isDeleteFile: Boolean): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_DELETE
                this.putExtra(EXTRA_PARAM_ACTION, taskIds)
                this.putExtra(EXTRA_PARAM_IS_DELETE, isDeleteFile)
            }
        }

        fun newDeleteAllIntent(mContext: Context, clazz: Class<*>): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_DELETE_ALL
            }
        }

        fun newRenameIntent(mContext: Context, clazz: Class<*>, taskId: String, fileName: String): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_FILE_RENAME
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
                this.putExtra(EXTRA_PARAM_FILE_NAME, fileName)
            }
        }
    }

    private fun initialService() {
        DownloadManager.isInitDownloadServiceCompat = false
        TaskManager.instance.setDownloadListener(customDownloadListener4WithSpeed)
        initialData(true)
    }

    private fun getCustomTaskListener() = object : CustomDownloadListener4WithSpeed.TaskListener {
        override fun onStart(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus) {
            downloadTask?.apply {
                this.taskSpeed = String()
                this.notificationId = "${this.url}${this.absolutePath}${this.id}".hashCode()
                this.downloadTaskStatus = downloadTaskStatus
                if (!FsUtils.exists(this.absolutePath)) {
                    this.currentOffset = 0
                    this.totalLength = 0
                }
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onStart ${this.absolutePath} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onInfoReady(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus, totalLength: Long) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.totalLength = totalLength
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onInfoReady ${this.absolutePath} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onProgress(downloadTask: DownloadTask?, task: OkDownloadTask, taskSpeed: String, downloadTaskStatus: DownloadTaskStatus, currentOffset: Long) {
            downloadTask?.apply {
                this.taskSpeed = taskSpeed
                this.currentOffset = currentOffset
                this.downloadTaskStatus = downloadTaskStatus
                val downloadPercent = CommonUtils.formatPercentInfo(this.currentOffset, this.totalLength)
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onProgress ${this.absolutePath} $downloadPercent ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onCancel(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onCancel ${this.absolutePath} ${this.currentOffset} ${this.totalLength} ${this.notificationId}")
            }
        }

        override fun onSuccess(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onSuccess ${this.absolutePath} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onError(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onError ${this.absolutePath} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onRetry(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus, retryCount: Int) {
            downloadTask?.apply {
                Logger.d(logTag, "onRetry ${this.absolutePath}  $retryCount")
            }
        }
    }

    fun handlerIntent(intent: Intent) {
        when (intent.action) {
            ActionType.ACTION_INIT -> {
                initialService()
            }
            ActionType.ACTION_UPDATE_TASK_DATA -> {
                initialData(false)
            }
            ActionType.ACTION_NEW_START -> {
                intent.getParcelableExtra<DownloadTask>(EXTRA_PARAM_ACTION)?.apply {
                    startNewTask(this)
                }
            }
            ActionType.ACTION_STOP -> {
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    stop(this)
                }
            }
            ActionType.ACTION_RESUME -> {
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    resume(this)
                }
            }
            ActionType.ACTION_DELETE -> {
                val isDeleteFile = intent.getBooleanExtra(EXTRA_PARAM_IS_DELETE, false)
                intent.getStringArrayListExtra(EXTRA_PARAM_ACTION)?.apply {
                    delete(this, isDeleteFile)
                }
            }
            ActionType.ACTION_START_ALL -> {
                startAll()
            }
            ActionType.ACTION_STOP_ALL -> {
                stopAll()
            }
            ActionType.ACTION_DELETE_ALL -> {
                deleteAll()
            }
            ActionType.ACTION_FILE_RENAME -> {
                val taskId = intent.getStringExtra(EXTRA_PARAM_ACTION) ?: return
                val fileName = intent.getStringExtra(EXTRA_PARAM_FILE_NAME) ?: return
                renameTaskFile(taskId, fileName)
            }
        }
    }

    private fun initialData(isInitialService: Boolean) {
        if (isInitialService) {
            DownloadManager.isInitDownloadServiceCompat = false
        }
        try {
            GlobalScope.launch(Dispatchers.Main) {
                val initTask = withContext(Dispatchers.IO) { AppDbHelper.queryInitDownloadTask() }
                Logger.d(logTag, "initialData task size ${initTask.allTasks.size}")
                downloadTaskLists.clear()
                downloadTaskLists.apply {
                    this.addAll(initTask.allTasks)
                }
                initTask.downloadIngTasks.forEach {
                    notifyHelper.cancel(it.notificationId)
                }
                if (isInitialService) {
                    DownloadManager.isInitDownloadServiceCompat = true
                    DownloadManager.downloadServiceInitCallback?.loadCompat()
                }
                DownloadManager.downloadTaskUpdateDataCallback?.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (isInitialService) {
                DownloadManager.isInitDownloadServiceCompat = false
            }
            GlobalScope.launch(Dispatchers.Main) {
                DownloadManager.downloadTaskUpdateDataCallback?.failed()
            }
        }
    }

    private fun startNewTask(downloadTask: DownloadTask) {
        if (downloadTask.url.isEmpty()) {
            return
        }
        val downloadTask1 = reformTaskData(downloadTask)
        removeOverRideDownloadFile(downloadTask1)
        DownloadManager.getDownloadTask(downloadTask1.id) ?: let {
            downloadTaskLists.add(0, downloadTask1)
        }
        TaskManager.instance.start(downloadTask1)
    }

    private fun reformTaskData(downloadTask: DownloadTask): DownloadTask {
        val url = downloadTask.url
        val downloadDir = if (downloadTask.absolutePath.isEmpty()) {
            FsUtils.getDefaultDownloadDir()
        } else {
            File(downloadTask.absolutePath).parentFile ?: FsUtils.getDefaultDownloadDir()
        }
        val tempFileName = if (downloadTask.tempFileName.isNotEmpty()) {
            downloadTask.tempFileName
        } else if (downloadTask.absolutePath.isNotEmpty()) {
            File(downloadTask.absolutePath).name
        } else {
            if (url.contains("/")) {
                URLUtil.guessFileName(url, null, null)
            } else {
                url.hashCode().toString()
            }
        }
        val taskFile = if (!downloadTask.overrideTaskFile) {
            CommonUtils.createAvailableFileName(File(downloadDir, tempFileName))
        } else {
            File(downloadDir, tempFileName)
        }
        downloadTask.tempFileName = tempFileName
        downloadTask.absolutePath = taskFile.absolutePath
        downloadTask.id = "${downloadTask.absolutePath.hashCode()}"
        //防止 之前任务是覆盖下载 这个任务是不覆盖下载 导致Id不一样数据库有2个相同任务
        return downloadTask
    }

    private fun removeOverRideDownloadFile(downloadTask: DownloadTask) {
        if (downloadTask.overrideTaskFile) {
            val taskFile = File(downloadTask.absolutePath)
            if (FsUtils.exists(taskFile) && taskFile.isFile) {
                DownloadManager.getDownloadTask(downloadTask.id)?.let {
                    TaskManager.instance.stop(downloadTask.id)
                }
                FsUtils.deleteFileOrDir(taskFile)
            }
        }
    }

    private fun startAll() {
        TaskManager.instance.startOnParallel()
    }

    private fun stop(taskId: String) {
        TaskManager.instance.stop(taskId)
        DownloadManager.getDownloadTask(taskId)?.let {
            if (it.showNotification) {
                notifyHelper.cancel(it.notificationId)
            }
        }
    }

    private fun resume(taskId: String) {
        TaskManager.instance.resume(taskId)
        DownloadManager.getDownloadTask(taskId)?.let {
            if (it.showNotification) {
                notifyHelper.cancel(it.notificationId)
            }
        }
    }

    private fun stopAll() {
        TaskManager.instance.stopAll()
        downloadTaskLists.forEach {
            if (it.showNotification) {
                notifyHelper.cancel(it.notificationId)
            }
        }
    }

    private fun deleteAll() {
        try {
            TaskManager.instance.deleteAll()
            AppDbHelper.deleteAllTasks()
            val missionList = arrayListOf<DownloadTask>()
            downloadTaskLists.forEach {
                missionList.add(it)
            }
            missionList.forEach {
                if (it.showNotification) {
                    notifyHelper.cancel(it.notificationId)
                }
                FsUtils.deleteFileOrDir(it.absolutePath)
            }
            missionList.forEach {
                DownloadTaskChangeLister.sendChangeBroadcast(mContext1, it.apply {
                    this.downloadTaskStatus = DownloadTaskStatus.Delete
                })
            }
            missionList.clear()
            downloadTaskLists.clear()
            DownloadTaskFileChangeLister.sendDeleteBroadcast(mContext1, missionList, true)
        } catch (e: Exception) {
            e.printStackTrace()
            DownloadTaskFileChangeLister.sendDeleteBroadcast(mContext1, null, false)
        }
    }

    private fun delete(taskIds: ArrayList<String>, isDeleteFile: Boolean) {
        val downloadTaskBeanList1 = arrayListOf<DownloadTask>()
        taskIds.forEach { it1 ->
            DownloadManager.getDownloadTask(it1)?.let { it2 ->
                TaskManager.instance.delete(it2.id)
                downloadTaskBeanList1.add(it2)
                downloadTaskLists.remove(it2)
            }
        }
        if (downloadTaskBeanList1.isEmpty()) {
            return
        }
        try {
            AppDbHelper.deleteTasks(downloadTaskBeanList1)
            downloadTaskBeanList1.forEach {
                if (isDeleteFile) {
                    FsUtils.deleteFileOrDir(it.absolutePath)
                }
                if (it.showNotification) {
                    notifyHelper.cancel(it.notificationId)
                }
                it.downloadTaskStatus = DownloadTaskStatus.Delete
                DownloadTaskChangeLister.sendChangeBroadcast(mContext1, it)
            }
            DownloadTaskFileChangeLister.sendDeleteBroadcast(mContext1, downloadTaskBeanList1, true)
        } catch (e: Exception) {
            e.printStackTrace()
            DownloadTaskFileChangeLister.sendDeleteBroadcast(mContext1, downloadTaskBeanList1, false)
        }
    }

    private fun renameTaskFile(taskId: String, fileName: String) {
        val downloadTask = DownloadManager.getDownloadTask(taskId)
        if (downloadTask == null || !FsUtils.exists(downloadTask.absolutePath)
                || downloadTask.downloadTaskStatus != DownloadTaskStatus.Success
                || fileName.isEmpty()) {
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTask, false)
            return
        }
        if (fileName == File(downloadTask.absolutePath).name) {
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTask, true)
            return
        }
        val newFile = FsUtils.renameFile(File(downloadTask.absolutePath), fileName)
        if (!FsUtils.exists(newFile)) {
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTask, false)
            return
        }
        downloadTask.absolutePath = newFile!!.absolutePath
        downloadTask.tempFileName = fileName
        try {
            AppDbHelper.createOrUpdateDownloadTask(downloadTask)
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTask, true)
        } catch (e: Exception) {
            e.printStackTrace()
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTask, false)
        }
    }

    private fun updateDbDataAndNotify(downloadTask: DownloadTask) {
        try {
            AppDbHelper.createOrUpdateDownloadTask(downloadTask)
            DownloadTaskChangeLister.sendChangeBroadcast(mContext1, downloadTask)
            if (downloadTask.showNotification) {
                downloadTask.downloadTaskStatus.let {
                    when (it) {
                        DownloadTaskStatus.Waiting -> notifyHelper.hintTaskIngNotify(downloadTask)
                        DownloadTaskStatus.Preparing -> notifyHelper.hintTaskIngNotify(downloadTask)
                        DownloadTaskStatus.Stop -> {
                        }
                        DownloadTaskStatus.Downloading -> notifyHelper.hintTaskIngNotify(downloadTask)
                        DownloadTaskStatus.Success -> {
                            notifyHelper.hintDownloadCompleteNotify(downloadTask)
                        }
                        DownloadTaskStatus.Failed -> notifyHelper.hintDownloadFailed(downloadTask)
                        else -> {
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}