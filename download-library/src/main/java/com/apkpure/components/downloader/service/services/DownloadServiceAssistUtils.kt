package com.apkpure.components.downloader.service.services

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.apkpure.components.downloader.R
import com.apkpure.components.downloader.db.AppDbHelper
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import com.apkpure.components.downloader.db.enums.DownloadTaskStatusType
import com.apkpure.components.downloader.service.misc.CustomDownloadListener4WithSpeed
import com.apkpure.components.downloader.service.misc.DownloadManager
import com.apkpure.components.downloader.utils.*
import com.liulishuo.okdownload.DownloadTask

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
        initDownloadTask()
    }

    private fun initDownloadTask() {
        DownloadManager.instance.apply {
            this.setTaskListener(object :
                    CustomDownloadListener4WithSpeed.TaskListener {
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
                        val downloadPercent = FormatUtils.formatPercentInfo(this.currentOffset, this.totalLength)
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
            })
        }
    }

    fun handlerIntent(intent: Intent) {
        when (intent.action) {
            ActionType.ACTION_START -> {
                intent.getParcelableExtra<DownloadTaskBean>(EXTRA_PARAM_ACTION)?.apply {
                    DownloadManager.instance.start(this)
                }
            }
            ActionType.ACTION_STOP -> {
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    DownloadManager.instance.stop(this, true)
                }
            }
            ActionType.ACTION_DELETE -> {
                val isDeleteFile = intent.getBooleanExtra(EXTRA_PARAM_IS_DELETE, false)
                intent.getStringExtra(EXTRA_PARAM_ACTION)?.apply {
                    DownloadManager.instance.delete(this, isDeleteFile, true)
                }
            }
            ActionType.ACTION_START_ALL -> {
                DownloadManager.instance.startAll()
            }
            ActionType.ACTION_STOP_ALL -> {
                DownloadManager.instance.stopAll(true)
            }
            ActionType.ACTION_DELETE_ALL -> {
                DownloadManager.instance.deleteAll(isDeleteFile = true, isCancelNotify = true)
            }
        }
    }

    private fun updateDbDataAndNotify(downloadTaskBean: DownloadTaskBean) {
        AppDbHelper.instance
                .createOrUpdateDownloadTask(downloadTaskBean)
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        EventManager.post(downloadTaskBean)
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
                ?: NotificationCompat.Builder(mContext1, IdentifierUtils.notificationChannelId)
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
                ?: NotificationCompat.Builder(mContext1, IdentifierUtils.notificationChannelId)
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
                ?: NotificationCompat.Builder(mContext1, IdentifierUtils.notificationChannelId)
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