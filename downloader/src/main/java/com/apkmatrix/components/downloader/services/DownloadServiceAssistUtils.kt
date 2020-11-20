package com.apkmatrix.components.downloader.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import com.apkmatrix.components.downloader.DownloadManager
import com.apkmatrix.components.downloader.db.AppDbHelper
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import com.apkmatrix.components.downloader.misc.*
import com.apkmatrix.components.downloader.utils.CommonUtils
import com.apkmatrix.components.downloader.utils.FsUtils
import com.apkmatrix.components.downloader.utils.Logger
import com.apkmatrix.components.downloader.utils.NotifyHelper
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * @author xiongke
 * @date 2019/1/23
 */
internal class DownloadServiceAssistUtils(private val mService: Service) {
    private val mContext1 by lazy { mService }
    private val logTag by lazy { mService.javaClass.simpleName }
    private val notifyHelper by lazy { NotifyHelper(mService) }
    private val customDownloadListener4WithSpeed by lazy {
        CustomDownloadListener4WithSpeed().apply { this.setTaskListener(getCustomTaskListener()) }
    }
    private var serviceIngState = ServiceIngState.End

    private enum class ServiceIngState {
        Ing,
        End
    }

    companion object {
        private const val EXTRA_PARAM_ACTION = "download_param_action"
        private const val EXTRA_PARAM_IS_DELETE = "is_delete"
        private const val EXTRA_PARAM_FILE_NAME = "file_name"
        var isInitServiceComplete = false

        object ActionType {
            const val ACTION_EMPTY = "empty"
            const val ACTION_UPDATE_TASK_DATA = "update_task_data"
            const val ACTION_NEW_START = "new_start"
            const val ACTION_STOP = "stop"
            const val ACTION_RESUME = "resume"
            const val ACTION_DELETE = "delete"
            const val ACTION_DELETE_ALL = "delete_all"
            const val ACTION_FILE_RENAME = "file_rename"
        }

        fun newEmptyIntent(mContext: Context): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_EMPTY
            }
        }

        fun newUpdateTaskDataIntent(mContext: Context): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_UPDATE_TASK_DATA
            }
        }

        fun newStartNewTaskIntent(mContext: Context, downloadTask: DownloadTask): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_NEW_START
                this.putExtra(EXTRA_PARAM_ACTION, downloadTask)
            }
        }

        fun newStopIntent(mContext: Context, taskId: String): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_STOP
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
            }
        }

        fun newResumeIntent(mContext: Context, taskId: String): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_RESUME
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
            }
        }

        fun newDeleteIntent(mContext: Context, taskId: String, isDeleteFile: Boolean): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_DELETE
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
                this.putExtra(EXTRA_PARAM_IS_DELETE, isDeleteFile)
            }
        }

        fun newDeleteAllIntent(mContext: Context): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_DELETE_ALL
            }
        }

        fun newRenameIntent(mContext: Context, taskId: String, fileName: String): Intent {
            return Intent(mContext, DownloadService::class.java).apply {
                this.action = ActionType.ACTION_FILE_RENAME
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
                this.putExtra(EXTRA_PARAM_FILE_NAME, fileName)
            }
        }
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
                Logger.d(logTag, "onStart ${this.id} ${this.downloadTaskStatus.name} ${this.notificationTitle} ${this.absolutePath}")
            }
        }

        override fun onInfoReady(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus, totalLength: Long) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.totalLength = totalLength
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onInfoReady ${this.id} ${this.notificationTitle} ${this.absolutePath}")
            }
        }

        override fun onProgress(downloadTask: DownloadTask?, task: OkDownloadTask, taskSpeed: String, downloadTaskStatus: DownloadTaskStatus, currentOffset: Long) {
            downloadTask?.apply {
                this.taskSpeed = taskSpeed
                this.currentOffset = currentOffset
                this.downloadTaskStatus = downloadTaskStatus
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onProgress ${this.id} ${this.notificationTitle} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onCancel(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onCancel ${this.id} ${this.notificationTitle}")
            }
        }

        override fun onSuccess(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onSuccess ${this.id} ${this.notificationTitle} ${this.absolutePath}")
            }
        }

        override fun onError(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus) {
            downloadTask?.apply {
                this.downloadTaskStatus = downloadTaskStatus
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                Logger.d(logTag, "onError ${this.id} ${this.notificationTitle}")
            }
        }

        override fun onRetry(downloadTask: DownloadTask?, task: OkDownloadTask, downloadTaskStatus: DownloadTaskStatus, retryCount: Int) {
            downloadTask?.apply {
                Logger.d(logTag, "onRetry ${this.id} ${this.notificationTitle}  $retryCount")
            }
        }
    }

    fun initialService() {
        if (isInitServiceComplete) {
            Logger.d(logTag, "service initial complete")
            return
        }
        if (serviceIngState == ServiceIngState.Ing) {
            Logger.d(logTag, "service initial ing")
            return
        }
        serviceIngState = ServiceIngState.Ing
        isInitServiceComplete = false
        GlobalScope.launch(Dispatchers.Main) {
            try {
                Logger.d(logTag, "service initial start")
                notifyHelper.init()
                TaskManager.instance.setDownloadListener(customDownloadListener4WithSpeed)
                val tasks = getDbTaskData()
                tasks.forEach {
                    notifyHelper.cancel(it.notificationId)
                }
                DownloadDataManager.instance.clear()
                DownloadDataManager.instance.addAll(tasks)
                isInitServiceComplete = true
                DownloadManager.downloadServiceInitCallback?.loadComplete()
                Logger.d(logTag, "service initial end task size: ${DownloadDataManager.instance.size()}")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                serviceIngState = ServiceIngState.End
            }
        }
    }

    fun handlerIntent(intent: Intent) {
        val action = intent.action
        if (action.isNullOrEmpty()) {
            return
        }
        if (action == ActionType.ACTION_EMPTY) {
            return
        }
        if (action == ActionType.ACTION_UPDATE_TASK_DATA) {
            updateTaskData()
            return
        }
        if (!isInitServiceComplete) {
            Logger.d(logTag, "handlerIntent service initial")
            initialService()
            return
        }
        if (!isInitServiceComplete) {
            Logger.d(logTag, "service not initial")
            return
        }
        Logger.d(logTag, action)
        when (action) {
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
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    delete(this, isDeleteFile)
                }
            }
            ActionType.ACTION_DELETE_ALL -> {
                deleteAll()
            }
            ActionType.ACTION_FILE_RENAME -> {
                val taskId = intent.getStringExtra(EXTRA_PARAM_ACTION) ?: return
                val fileName = intent.getStringExtra(EXTRA_PARAM_FILE_NAME) ?: return
                renameTaskFile(taskId, fileName)
            }
            else -> {

            }
        }
    }

    private suspend fun getDbTaskData(): List<DownloadTask> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { it1 ->
                it1.invokeOnCancellation {
                    it1.cancel()
                }
                val allTasks = AppDbHelper.queryInitDownloadTask().allTasks
                it1.resume(allTasks)
            }
        }
    }

    private fun updateTaskData() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val tasks = getDbTaskData()
                DownloadDataManager.instance.clear()
                DownloadDataManager.instance.addAll(tasks)
                DownloadManager.downloadTaskUpdateDataCallback?.success()
            } catch (e: Exception) {
                e.printStackTrace()
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
            DownloadDataManager.instance.add(0, downloadTask1)
        }
        TaskManager.instance.start(downloadTask1)
        Logger.d(logTag, "startNewTask ${downloadTask.id} ${downloadTask.notificationTitle}")
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

    private fun deleteAll() {
        try {
            TaskManager.instance.deleteAll()
            AppDbHelper.deleteAllTasks()
            val tempList = arrayListOf<DownloadTask>()
            DownloadDataManager.instance.getAll().forEach {
                tempList.add(it)
            }
            tempList.forEach {
                if (it.showNotification) {
                    notifyHelper.cancel(it.notificationId)
                }
                FsUtils.deleteFileOrDir(it.absolutePath)
            }
            tempList.forEach {
                it.apply {
                    this.downloadTaskStatus = DownloadTaskStatus.Delete
                }
                DownloadTaskChangeReceiver.sendChangeBroadcast(mContext1, it)
                DownloadTaskFileReceiver.sendDeleteBroadcast(mContext1, it, true)
            }
            tempList.clear()
            DownloadDataManager.instance.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun delete(taskId: String, isDeleteFile: Boolean) {
        var deleteTask: DownloadTask? = null
        try {
            DownloadManager.getDownloadTask(taskId)?.let {
                TaskManager.instance.delete(it.id)
                deleteTask = it
            }
            if (deleteTask == null) {
                return
            }
            DownloadDataManager.instance.remove(deleteTask!!)
            AppDbHelper.deleteTasks(arrayListOf(deleteTask!!))
            if (isDeleteFile) {
                FsUtils.deleteFileOrDir(deleteTask!!.absolutePath)
            }
            if (deleteTask!!.showNotification) {
                notifyHelper.cancel(deleteTask!!.notificationId)
            }
            deleteTask!!.downloadTaskStatus = DownloadTaskStatus.Delete
            DownloadTaskChangeReceiver.sendChangeBroadcast(mContext1, deleteTask!!)
            DownloadTaskFileReceiver.sendDeleteBroadcast(mContext1, deleteTask, true)
        } catch (e: Exception) {
            e.printStackTrace()
            DownloadTaskFileReceiver.sendDeleteBroadcast(mContext1, deleteTask, false)
        }
    }

    private fun renameTaskFile(taskId: String, fileName: String) {
        val downloadTask = DownloadManager.getDownloadTask(taskId)
        if (downloadTask == null || !FsUtils.exists(downloadTask.absolutePath)
                || downloadTask.downloadTaskStatus != DownloadTaskStatus.Success
                || fileName.isEmpty()) {
            DownloadTaskFileReceiver.sendRenameBroadcast(mContext1, downloadTask, false)
            return
        }
        if (fileName == File(downloadTask.absolutePath).name) {
            DownloadTaskFileReceiver.sendRenameBroadcast(mContext1, downloadTask, true)
            return
        }
        val newFile = FsUtils.renameFile(File(downloadTask.absolutePath), fileName)
        if (!FsUtils.exists(newFile)) {
            DownloadTaskFileReceiver.sendRenameBroadcast(mContext1, downloadTask, false)
            return
        }
        downloadTask.absolutePath = newFile!!.absolutePath
        downloadTask.tempFileName = fileName
        try {
            AppDbHelper.createOrUpdateDownloadTask(downloadTask)
            DownloadTaskFileReceiver.sendRenameBroadcast(mContext1, downloadTask, true)
        } catch (e: Exception) {
            e.printStackTrace()
            DownloadTaskFileReceiver.sendRenameBroadcast(mContext1, downloadTask, false)
        }
    }

    private fun updateDbDataAndNotify(downloadTask: DownloadTask) {
        try {
            AppDbHelper.createOrUpdateDownloadTask(downloadTask)
            DownloadTaskChangeReceiver.sendChangeBroadcast(mContext1, downloadTask)
            if (downloadTask.showNotification) {
                downloadTask.downloadTaskStatus.let {
                    when (it) {
                        DownloadTaskStatus.Waiting -> notifyHelper.hintTaskIngNotify(downloadTask)
                        DownloadTaskStatus.Preparing -> notifyHelper.hintTaskIngNotify(downloadTask)
                        DownloadTaskStatus.Stop -> {
                        }
                        DownloadTaskStatus.Downloading -> notifyHelper.hintTaskIngNotify(downloadTask)
                        DownloadTaskStatus.Success -> notifyHelper.hintDownloadCompleteNotify(downloadTask)
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

    fun destroy() {
        notifyHelper.destroy()
    }
}