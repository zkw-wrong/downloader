package com.apkpure.components.downloader.service.services

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.apkpure.components.downloader.R
import com.apkpure.components.downloader.db.AppDbHelper
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import com.apkpure.components.downloader.db.enums.DownloadTaskStatusType
import com.apkpure.components.downloader.service.DownloadManager
import com.apkpure.components.downloader.service.misc.CustomDownloadListener4WithSpeed
import com.apkpure.components.downloader.service.misc.DownloadTaskChangeLister
import com.apkpure.components.downloader.service.misc.DownloadTaskFileChangeLister
import com.apkpure.components.downloader.service.misc.TaskManager
import com.apkpure.components.downloader.utils.*
import com.liulishuo.okdownload.DownloadTask
import io.reactivex.disposables.Disposable
import java.io.File

/**
 * @author xiongke
 * @date 2019/1/23
 */
class DownloadServiceAssistUtils(private val mContext1: Context, clazz: Class<*>) {
    private val logTag by lazy { clazz.simpleName }
    private val notifyHelper by lazy { NotifyHelper(mContext1) }
    private var downloadIngNotification: NotificationCompat.Builder? = null
    private var downloadCompatNotification: NotificationCompat.Builder? = null
    private var downloadFailedNotification: NotificationCompat.Builder? = null
    private val customDownloadListener4WithSpeed by lazy {
        CustomDownloadListener4WithSpeed().apply { this.setTaskListener(getCustomTaskListener()) }
    }

    companion object {
        private const val EXTRA_PARAM_ACTION = "download_param_action"
        private const val EXTRA_PARAM_IS_DELETE = "is_delete"
        private const val EXTRA_PARAM_FILE_NAMNE = "file_name"
        val downloadTaskLists = mutableListOf<DownloadTaskBean>()

        object ActionType {
            const val ACTION_INIT = "init"
            const val ACTION_START = "start"
            const val ACTION_STOP = "stop"
            const val ACTION_DELETE = "delete"
            const val ACTION_START_ALL = "start_all"
            const val ACTION_STOP_ALL = "stop_all"
            const val ACTION_DELETE_ALL = "delete_all"
            const val ACTION_FILE_RENAME = "file_rename"
        }

        fun newInitIntent(mContext: Context, clazz: Class<*>): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_INIT
            }
        }

        fun newStartIntent(mContext: Context, clazz: Class<*>, downloadTaskBean: DownloadTaskBean): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_START
                this.putExtra(EXTRA_PARAM_ACTION, downloadTaskBean)
            }
        }

        fun newStopIntent(mContext: Context, clazz: Class<*>, taskUrl: String): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_STOP
                this.putExtra(EXTRA_PARAM_ACTION, taskUrl)
            }
        }

        fun newDeleteIntent(mContext: Context, clazz: Class<*>, taskUrl: String, isDeleteFile: Boolean): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_DELETE
                this.putExtra(EXTRA_PARAM_ACTION, taskUrl)
                this.putExtra(EXTRA_PARAM_IS_DELETE, isDeleteFile)
            }
        }

        fun newDeleteAllIntent(mContext: Context, clazz: Class<*>): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_DELETE_ALL
            }
        }

        fun newRenameIntent(mContext: Context, clazz: Class<*>, taskUrl: String, fileName: String): Intent {
            return Intent(mContext, clazz).apply {
                this.action = ActionType.ACTION_FILE_RENAME
                this.putExtra(EXTRA_PARAM_ACTION, taskUrl)
                this.putExtra(EXTRA_PARAM_FILE_NAMNE, fileName)
            }
        }
    }

    private fun initial() {
        initialData()
        TaskManager.instance.setDownloadListener(customDownloadListener4WithSpeed)
    }

    private fun getCustomTaskListener() = object : CustomDownloadListener4WithSpeed.TaskListener {
        override fun onStart(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType) {
            downloadTaskBean?.apply {
                this.absolutePath = task.file?.path
                this.taskSpeed = String()
                this.notificationId = this.url.hashCode()
                this.downloadTaskStatusType = downloadTaskStatusType
                if (!FsUtils.exists(this.absolutePath)) {
                    this.currentOffset = 0
                    this.totalLength = 0
                }
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onStart ${this.notificationTitle} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onInfoReady(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType, totalLength: Long) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.totalLength = totalLength
                this.absolutePath = task.file?.path
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onInfoReady ${this.notificationTitle} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onProgress(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, taskSpeed: String, downloadTaskStatusType: DownloadTaskStatusType, currentOffset: Long) {
            downloadTaskBean?.apply {
                this.taskSpeed = taskSpeed
                this.currentOffset = currentOffset
                this.absolutePath = task.file?.path
                this.downloadTaskStatusType = downloadTaskStatusType
                val downloadPercent = CommonUtils.formatPercentInfo(this.currentOffset, this.totalLength)
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onProgress ${this.notificationTitle} ${task.connectionCount} ${this.currentOffset} ${this.totalLength} $downloadPercent")
            }
        }

        override fun onCancel(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.absolutePath = task.file?.path
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onCancel ${this.notificationTitle} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onSuccess(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.absolutePath = task.file?.path
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onSuccess ${this.notificationTitle} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onError(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.absolutePath = task.file?.path
                this.taskSpeed = String()
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onError ${this.notificationTitle} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onRetry(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType, retryCount: Int) {
            downloadTaskBean?.apply {
                AppLogger.d(logTag, "onRetry ${this.notificationTitle}  $retryCount")
            }
        }
    }

    fun handlerIntent(intent: Intent) {
        when (intent.action) {
            ActionType.ACTION_INIT -> {
                initial()
            }
            ActionType.ACTION_START -> {
                intent.getParcelableExtra<DownloadTaskBean>(EXTRA_PARAM_ACTION)?.apply {
                    start(this)
                }
            }
            ActionType.ACTION_STOP -> {
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    stop(this)
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
                val taskUrl = intent.getStringExtra(EXTRA_PARAM_ACTION) ?: return
                val fileName = intent.getStringExtra(EXTRA_PARAM_FILE_NAMNE) ?: return
                renameTaskFile(taskUrl, fileName)
            }
        }
    }

    private fun initialData() {
        AppDbHelper.instance.queryAllDownloadTask()
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<List<DownloadTaskBean>>() {
                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        downloadTaskLists.clear()
                    }

                    override fun rxOnNext(t: List<DownloadTaskBean>) {
                        AppLogger.d(logTag, "initialData task size ${t.size}")
                        downloadTaskLists.apply {
                            this.addAll(t)
                        }
                    }

                    override fun rxOnError(e: Exception) = Unit
                })
    }

    private fun start(downloadTaskBean: DownloadTaskBean) {
        DownloadManager.instance.getDownloadTask(downloadTaskBean.url) ?: let {
            downloadTaskLists.add(0, downloadTaskBean)
        }
        TaskManager.instance.start(downloadTaskBean)
    }

    private fun startAll() {
        TaskManager.instance.startOnParallel()
    }

    private fun stop(taskUrl: String) {
        TaskManager.instance.stop(taskUrl)
        DownloadManager.instance.getDownloadTask(taskUrl)?.let {
            if (it.showNotification) {
                notifyHelper.notificationManager.cancel(it.notificationId)
            }
        }
    }

    private fun stopAll() {
        TaskManager.instance.stopAll()
        downloadTaskLists.forEach {
            if (it.showNotification) {
                notifyHelper.notificationManager.cancel(it.notificationId)
            }
        }
    }

    private fun deleteAll() {
        TaskManager.instance.deleteAll()
        AppDbHelper.instance.deleteAllTasks()
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        val missionList = downloadTaskLists
                        missionList.forEach {
                            if (it.showNotification) {
                                notifyHelper.notificationManager.cancel(it.notificationId)
                            }
                            FsUtils.deleteFileOrDir(it.absolutePath)
                        }
                        missionList.forEach {
                            DownloadTaskChangeLister.sendChangeBroadcast(mContext1, it.apply {
                                this.downloadTaskStatusType = DownloadTaskStatusType.Delete
                            })
                        }
                        missionList.clear()
                        DownloadTaskFileChangeLister.sendAllDeleteBroadcast(mContext1, true)
                    }

                    override fun rxOnError(e: Exception) {
                        DownloadTaskFileChangeLister.sendAllDeleteBroadcast(mContext1, false)
                    }
                })
    }

    private fun delete(taskUrl: String, isDeleteFile: Boolean) {
        val downloadTaskBean = DownloadManager.instance.getDownloadTask(taskUrl)
        if (downloadTaskBean == null) {
            DownloadTaskFileChangeLister.sendDeleteBroadcast(mContext1, downloadTaskBean, false)
            return
        }
        downloadTaskLists.remove(downloadTaskBean)
        TaskManager.instance.delete(downloadTaskBean.url)
        AppDbHelper.instance.deleteSingleMission(downloadTaskBean)
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        if (isDeleteFile) {
                            FsUtils.deleteFileOrDir(downloadTaskBean.absolutePath)
                        }
                        if (downloadTaskBean.showNotification) {
                            notifyHelper.notificationManager.cancel(downloadTaskBean.notificationId)
                        }
                        DownloadTaskChangeLister.sendChangeBroadcast(mContext1, downloadTaskBean.apply {
                            this.downloadTaskStatusType = DownloadTaskStatusType.Delete
                        })
                        DownloadTaskFileChangeLister.sendDeleteBroadcast(mContext1, downloadTaskBean, true)
                    }

                    override fun rxOnError(e: Exception) {
                        DownloadTaskFileChangeLister.sendDeleteBroadcast(mContext1, downloadTaskBean, false)
                    }
                })
    }

    private fun renameTaskFile(taskUrl: String, fileName: String) {
        val downloadTaskBean = DownloadManager.instance.getDownloadTask(taskUrl)
        if (downloadTaskBean == null || !FsUtils.exists(downloadTaskBean.absolutePath)
                || downloadTaskBean.downloadTaskStatusType != DownloadTaskStatusType.Success
                || fileName.isEmpty()) {
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTaskBean, false)
            return
        }
        if (fileName == File(downloadTaskBean.absolutePath).name) {
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTaskBean, true)
            return
        }
        val newFile = FsUtils.renameFile(File(downloadTaskBean.absolutePath), fileName)
        if (!FsUtils.exists(newFile)) {
            DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTaskBean, false)
            return
        }
        downloadTaskBean.absolutePath = newFile!!.absolutePath
        AppDbHelper.instance
                .createOrUpdateDownloadTask(downloadTaskBean)
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTaskBean, true)
                    }

                    override fun rxOnError(e: Exception) {
                        DownloadTaskFileChangeLister.sendRenameBroadcast(mContext1, downloadTaskBean, false)
                    }
                })
    }

    private fun updateDbDataAndNotify(downloadTaskBean: DownloadTaskBean) {
        AppDbHelper.instance
                .createOrUpdateDownloadTask(downloadTaskBean)
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        DownloadTaskChangeLister.sendChangeBroadcast(mContext1, downloadTaskBean)
                        if (downloadTaskBean.showNotification && downloadTaskBean.notificationId != -1) {
                            downloadTaskBean.downloadTaskStatusType?.let {
                                when (it) {
                                    DownloadTaskStatusType.Waiting -> hintTaskIngNotify(downloadTaskBean)
                                    DownloadTaskStatusType.Preparing -> hintTaskIngNotify(downloadTaskBean)
                                    DownloadTaskStatusType.Stop -> {
                                    }
                                    DownloadTaskStatusType.Downloading -> hintTaskIngNotify(downloadTaskBean)
                                    DownloadTaskStatusType.Success -> {
                                        hintDownloadCompleteNotify(downloadTaskBean)
                                    }
                                    DownloadTaskStatusType.Failed -> hintDownloadFailed(downloadTaskBean)
                                    else -> {
                                    }
                                }
                            }
                        }
                    }

                    override fun rxOnError(e: Exception) = Unit
                })
    }

    private fun hintTaskIngNotify(downloadTaskBean: DownloadTaskBean) {
        downloadIngNotification = downloadIngNotification
                ?: NotificationCompat.Builder(mContext1, CommonUtils.notificationChannelId)
                        .setSmallIcon(R.drawable.download_status_downloading)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setShowWhen(false)
        downloadIngNotification?.apply {
            if (!downloadTaskBean.notificationTitle.isNullOrEmpty()) {
                this.setContentTitle(downloadTaskBean.notificationTitle)
            }
            this.setContentText(CommonUtils.downloadStateNotificationInfo(mContext1, downloadTaskBean))
            this.setProgress(downloadTaskBean.totalLength.toInt(), downloadTaskBean.currentOffset.toInt(), false)
            notifyHelper.notificationManager.notify(downloadTaskBean.notificationId, this.build())
        }
    }

    private fun hintDownloadCompleteNotify(downloadTaskBean: DownloadTaskBean) {
        downloadCompatNotification = downloadCompatNotification
                ?: NotificationCompat.Builder(mContext1, CommonUtils.notificationChannelId)
                        .setSmallIcon(R.drawable.ic_apk_status_complete)
                        .setContentTitle(mContext1.getString(R.string.q_download_complete))
                        .setOngoing(false)
                        .setAutoCancel(true)
        downloadCompatNotification?.apply {
            if (!downloadTaskBean.notificationTitle.isNullOrEmpty()) {
                this.setContentTitle(downloadTaskBean.notificationTitle)
            }
            this.setContentText(CommonUtils.downloadStateNotificationInfo(mContext1, downloadTaskBean))
            notifyHelper.notificationManager.cancel(downloadTaskBean.notificationId)
            notifyHelper.notificationManager.notify(downloadTaskBean.notificationId, this.build())
        }
    }

    private fun hintDownloadFailed(downloadTaskBean: DownloadTaskBean) {
        downloadFailedNotification = downloadFailedNotification
                ?: NotificationCompat.Builder(mContext1, CommonUtils.notificationChannelId)
                        .setSmallIcon(R.drawable.download_status_failed)
                        .setOngoing(false)
                        .setAutoCancel(true)
        downloadFailedNotification?.apply {
            if (!downloadTaskBean.notificationTitle.isNullOrEmpty()) {
                this.setContentTitle(downloadTaskBean.notificationTitle)
            }
            this.setContentText(CommonUtils.downloadStateNotificationInfo(mContext1, downloadTaskBean))
            notifyHelper.notificationManager.cancel(downloadTaskBean.notificationId)
            notifyHelper.notificationManager.notify(downloadTaskBean.notificationId, this.build())
        }
    }
}