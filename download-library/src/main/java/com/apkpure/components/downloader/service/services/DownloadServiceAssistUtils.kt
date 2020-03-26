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
import com.apkpure.components.downloader.service.misc.DownloadTaskDeleteLister
import com.apkpure.components.downloader.service.misc.TaskManager
import com.apkpure.components.downloader.utils.*
import com.liulishuo.okdownload.DownloadTask
import io.reactivex.disposables.Disposable

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
        CustomDownloadListener4WithSpeed()
                .apply { this.setTaskListener(getCustomTaskListener()) }
    }

    companion object {
        private const val EXTRA_PARAM_ACTION = "download_param_action"
        private const val EXTRA_PARAM_IS_DELETE = "is_delete"

        object ActionType {
            const val ACTION_START = "start"
            const val ACTION_STOP = "stop"
            const val ACTION_DELETE = "delete"
            const val ACTION_START_ALL = "start_all"
            const val ACTION_STOP_ALL = "stop_all"
            const val ACTION_DELETE_ALL = "delete_all"
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
    }

    fun initial() {
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
                AppLogger.d(logTag, "onStart ${this.shortName} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onInfoReady(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType, totalLength: Long) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.totalLength = totalLength
                this.absolutePath = task.file?.path
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onInfoReady ${this.shortName} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
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
                AppLogger.d(logTag, "onProgress ${this.shortName} ${task.connectionCount} ${this.currentOffset} ${this.totalLength} $downloadPercent")
            }
        }

        override fun onCancel(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.absolutePath = task.file?.path
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onCancel ${this.shortName} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onSuccess(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.absolutePath = task.file?.path
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onSuccess ${this.shortName} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onError(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType) {
            downloadTaskBean?.apply {
                this.downloadTaskStatusType = downloadTaskStatusType
                this.absolutePath = task.file?.path
                updateDbDataAndNotify(this)
                AppLogger.d(logTag, "onError ${this.shortName} ${task.connectionCount} ${this.currentOffset} ${this.totalLength}")
            }
        }

        override fun onRetry(downloadTaskBean: DownloadTaskBean?, task: DownloadTask, downloadTaskStatusType: DownloadTaskStatusType, retryCount: Int) {
            downloadTaskBean?.apply {
                AppLogger.d(logTag, "onRetry ${this.shortName}  $retryCount")
            }
        }
    }

    fun handlerIntent(intent: Intent) {
        when (intent.action) {
            ActionType.ACTION_START -> {
                intent.getParcelableExtra<DownloadTaskBean>(EXTRA_PARAM_ACTION)?.apply {
                    start(this)
                }
            }
            ActionType.ACTION_STOP -> {
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    stop(this, true)
                }
            }
            ActionType.ACTION_DELETE -> {
                val isDeleteFile = intent.getBooleanExtra(EXTRA_PARAM_IS_DELETE, false)
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    delete(this, isDeleteFile, true)
                }
            }
            ActionType.ACTION_START_ALL -> {
                startAll()
            }
            ActionType.ACTION_STOP_ALL -> {
                stopAll(true)
            }
            ActionType.ACTION_DELETE_ALL -> {
                deleteAll(isDeleteFile = true, isCancelNotify = true)
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
                        DownloadManager.instance.downloadTaskLists.clear()
                    }

                    override fun rxOnNext(t: List<DownloadTaskBean>) {
                        DownloadManager.instance.downloadTaskLists.apply {
                            this.addAll(t)
                        }
                    }

                    override fun rxOnError(e: Exception) = Unit
                })
    }

    private fun start(downloadTaskBean: DownloadTaskBean) {
        DownloadManager.instance.getDownloadTask(downloadTaskBean.url) ?: let {
            DownloadManager.instance.downloadTaskLists.add(0, downloadTaskBean)
        }
        TaskManager.instance.start(downloadTaskBean.url, downloadTaskBean.absolutePath)
    }

    private fun startAll() {
        TaskManager.instance.startOnParallel()
    }

    private fun stop(taskUrl: String, isCancelNotify: Boolean) {
        TaskManager.instance.stop(taskUrl)
        if (isCancelNotify) {
            DownloadManager.instance.getDownloadTask(taskUrl)?.let {
                notifyHelper.notificationManager.cancel(it.notificationId)
            }
        }
    }

    private fun stopAll(isCancelNotify: Boolean) {
        TaskManager.instance.stopAll()
        DownloadManager.instance.downloadTaskLists.forEach {
            if (isCancelNotify) {
                notifyHelper.notificationManager.cancel(it.notificationId)
            }
        }
    }

    private fun deleteAll(isDeleteFile: Boolean, isCancelNotify: Boolean) {
        TaskManager.instance.deleteAll()
        AppDbHelper.instance.deleteAllTasks()
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        val missionList = DownloadManager.instance.downloadTaskLists
                        missionList.forEach {
                            if (isCancelNotify) {
                                notifyHelper.notificationManager.cancel(it.notificationId)
                            }
                            if (isDeleteFile) {
                                FsUtils.deleteFileOrDir(it.absolutePath)
                            }
                        }
                        missionList.forEach {
                            DownloadTaskChangeLister.sendChangeBroadcast(mContext1, it.apply {
                                this.downloadTaskStatusType = DownloadTaskStatusType.Delete
                            })
                        }
                        missionList.clear()
                        DownloadTaskDeleteLister.sendAllDeleteBroadcast(mContext1)
                    }

                    override fun rxOnError(e: Exception) = Unit
                })
    }

    private fun delete(taskUrl: String, isDeleteFile: Boolean, isCancelNotify: Boolean) {
        val downloadTaskBean = DownloadManager.instance.getDownloadTask(taskUrl) ?: return
        DownloadManager.instance.downloadTaskLists.remove(downloadTaskBean)
        TaskManager.instance.delete(downloadTaskBean.url)
        AppDbHelper.instance.deleteSingleMission(downloadTaskBean)
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        if (isDeleteFile) {
                            FsUtils.deleteFileOrDir(downloadTaskBean.absolutePath)
                        }
                        if (isCancelNotify) {
                            notifyHelper.notificationManager.cancel(downloadTaskBean.notificationId)
                        }
                        DownloadTaskDeleteLister.sendDeleteBroadcast(mContext1, TaskDeleteStatusEvent(TaskDeleteStatusEvent.Status.DELETE_SINGLE, downloadTaskBean))
                        DownloadTaskChangeLister.sendChangeBroadcast(mContext1, downloadTaskBean.apply {
                            this.downloadTaskStatusType = DownloadTaskStatusType.Delete
                        })
                    }

                    override fun rxOnError(e: Exception) = Unit
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
            downloadTaskBean.shortName?.let {
                this.setContentTitle(it)
            }
            // this.setContentText(DownloadUtils.downloadStateNotificationInfo(mContext1, missionDbBean))
            this.setProgress(downloadTaskBean.totalLength.toInt(), downloadTaskBean.currentOffset.toInt(), false)
            notifyHelper.notificationManager.notify(downloadTaskBean.notificationId, this.build())
        }
    }

    private fun hintDownloadCompleteNotify(downloadTaskBean: DownloadTaskBean) {
        downloadCompatNotification = downloadCompatNotification
                ?: NotificationCompat.Builder(mContext1, CommonUtils.notificationChannelId)
                        .setSmallIcon(R.drawable.ic_apk_status_complete)
                        .setContentTitle(mContext1.getString(R.string.download_complete))
                        .setOngoing(false)
                        .setAutoCancel(true)
        downloadCompatNotification?.apply {
            downloadTaskBean.shortName?.let {
                this.setContentTitle(it)
            }
            //this.setContentText(DownloadUtils.downloadStateNotificationInfo(mContext1, missionDbBean))
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
            downloadTaskBean.shortName?.let {
                this.setContentTitle(it)
            }
            //this.setContentText(DownloadUtils.downloadStateNotificationInfo(mContext1, missionDbBean))
            notifyHelper.notificationManager.cancel(downloadTaskBean.notificationId)
            notifyHelper.notificationManager.notify(downloadTaskBean.notificationId, this.build())
        }
    }
}