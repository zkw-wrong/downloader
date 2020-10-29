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
internal class DownloadServiceAssistUtils(private val mService: Service) {
    private val mContext1 by lazy { mService }
    private val logTag by lazy { mService.javaClass.simpleName }
    private val notifyHelper by lazy { NotifyHelper(mService) }
    private val customDownloadListener4WithSpeed by lazy {
        CustomDownloadListener4WithSpeed().apply { this.setTaskListener(getCustomTaskListener()) }
    }

    companion object {
        private const val ServiceFlag = "DownloadService"
        private const val EXTRA_PARAM_ACTION = "download_param_action"
        private const val EXTRA_PARAM_IS_DELETE = "is_delete"
        private const val EXTRA_PARAM_FILE_NAME = "file_name"
        var isInitDownloadServiceCompat = false

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

        fun newDeleteIntent(mContext: Context, clazz: Class<*>, taskId: String, isDeleteFile: Boolean): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_DELETE
                this.putExtra(EXTRA_PARAM_ACTION, taskId)
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
                Logger.d(logTag, "onStart ${this.id} ${this.notificationTitle} ${this.absolutePath}")
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
                Logger.d(logTag, "onProgress ${this.id} ${this.notificationTitle} ${this.notificationTitle} ${this.currentOffset} ${this.totalLength}")
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

    private fun initialService() {
        isInitDownloadServiceCompat = false
        DownloadDataManager.instance.clear()
        DownloadDataManager.instance.clear()
        notifyHelper.init()
        TaskManager.instance.setDownloadListener(customDownloadListener4WithSpeed)
        initialData(object : InitDataCallBack {
            override fun success(list: List<DownloadTask>) {
                isInitDownloadServiceCompat = true
                list.forEach {
                    notifyHelper.cancel(it.notificationId)
                }
                DownloadDataManager.instance.addAll(list)
                DownloadManager.downloadServiceInitCallback?.loadCompat()
                Logger.d(logTag, "initialService task size: ${ DownloadDataManager.instance.size()}")
            }

            override fun failed() {
                isInitDownloadServiceCompat = false
            }
        })
    }

    fun handlerIntent(intent: Intent) {
        if (intent.action == ActionType.ACTION_INIT) {
            initialService()
        } else if (intent.action == ActionType.ACTION_UPDATE_TASK_DATA) {
            updateTaskData()
        }
        if (!isInitDownloadServiceCompat) {
            return
        }
        when (intent.action) {
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
            else -> {

            }
        }
    }

    private fun initialData(initDataCallBack: InitDataCallBack) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val initTask = withContext(Dispatchers.IO) { AppDbHelper.queryInitDownloadTask() }
                initDataCallBack.success(initTask.allTasks)
            } catch (e: Exception) {
                e.printStackTrace()
                initDataCallBack.failed()
            }
        }
    }

    private fun updateTaskData() {
        initialData(object : InitDataCallBack {
            override fun success(list: List<DownloadTask>) {
                DownloadDataManager.instance.clear()
                DownloadDataManager.instance.addAll(list)
                DownloadManager.downloadTaskUpdateDataCallback?.success()
            }

            override fun failed() {
                DownloadManager.downloadTaskUpdateDataCallback?.failed()
            }
        })
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
        DownloadDataManager.instance.getAll().forEach {
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

    fun destroy() {
        notifyHelper.destroy()
    }
}