package com.apkpure.components.downloader.service.misc

import android.app.Application
import android.os.Build
import com.apkpure.components.downloader.db.AppDbHelper
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import com.apkpure.components.downloader.db.enums.DownloadTaskStatusType
import com.apkpure.components.downloader.service.services.KeepAliveJobService
import com.apkpure.components.downloader.utils.*
import okhttp3.OkHttpClient

/**
 * @author xiongke
 * @date 2018/11/16
 */
class DownloadManager {
    private val appDbHelper by lazy { AppDbHelper.instance }
    private val notifyHelper by lazy { NotifyHelper(application) }
    private val downloadTaskLists by lazy { mutableListOf<DownloadTaskBean>() }
    private val taskManager by lazy { TaskManager.instance }
    private val customDownloadListener4WithSpeed by lazy { CustomDownloadListener4WithSpeed() }
    private var customTaskListener: CustomDownloadListener4WithSpeed.TaskListener? = null

    companion object {
        private var downloadManager: DownloadManager? = null
        private lateinit var application: Application

        fun initial(application: Application, builder: OkHttpClient.Builder) {
            this.application = application
            AppDbHelper.init(application)
            TaskManager.init(application, builder)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                KeepAliveJobService.startJob(application)
            }
            instance.initialData()
        }

        val instance: DownloadManager
            get() {
                if (downloadManager == null) {
                    synchronized(DownloadManager::class.java) {
                        if (downloadManager == null) {
                            downloadManager = DownloadManager()
                        }
                    }
                }
                return downloadManager!!
            }
    }

    private fun initialData() {
        this.appDbHelper.queryAllDownloadTask()
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<List<DownloadTaskBean>>() {
                    override fun rxOnNext(t: List<DownloadTaskBean>) {
                        downloadTaskLists.apply {
                            this.clear()
                            this.addAll(t)
                        }
                    }

                    override fun rxOnError(e: Exception) = Unit
                })
        this.taskManager.setDownloadListener(customDownloadListener4WithSpeed)
    }

    fun setTaskListener(customTaskListener: CustomDownloadListener4WithSpeed.TaskListener) {
        this.customTaskListener = customTaskListener
        this.customDownloadListener4WithSpeed.setTaskListener(customTaskListener)
    }

    fun start(downloadTaskBean: DownloadTaskBean) {
        getDownloadTask(downloadTaskBean.url) ?: let {
            downloadTaskLists.add(0, downloadTaskBean)
        }
        taskManager.start(downloadTaskBean.url, downloadTaskBean.absolutePath)
    }

    fun getCompletedDownloadTask(): MutableList<DownloadTaskBean> {
        val completedPosts: MutableList<DownloadTaskBean> = mutableListOf()
        for (index in 0 until downloadTaskLists.size) {
            val downloadTaskBean = downloadTaskLists[index]
            if (downloadTaskBean.downloadTaskStatusType == DownloadTaskStatusType.Success) {
                completedPosts.add(downloadTaskBean)
            }
        }
        return completedPosts
    }

    fun getDownloadTask(taskUrl: String): DownloadTaskBean? {
        var downloadTaskBean: DownloadTaskBean? = null
        downloadTaskLists.iterator().forEach {
            if (it.url == taskUrl) {
                downloadTaskBean = it
            }
        }
        return downloadTaskBean
    }

    fun getDownloadTask() = this.downloadTaskLists

    fun getNotCompatDownloadTask() = this.downloadTaskLists.filter {
        it.downloadTaskStatusType == DownloadTaskStatusType.Waiting
                || it.downloadTaskStatusType == DownloadTaskStatusType.Preparing
                || it.downloadTaskStatusType == DownloadTaskStatusType.Downloading
    }

    fun startAll() {
        this.taskManager.startOnParallel()
    }

    fun stop(taskUrl: String, isCancelNotify: Boolean) {
        this.taskManager.stop(taskUrl)
        if (isCancelNotify) {
            getDownloadTask(taskUrl)?.let {
                notifyHelper.notificationManager.cancel(it.notificationId)
            }
        }
    }

    fun stopAll(isCancelNotify: Boolean) {
        this.taskManager.stopAll()
        getDownloadTask().forEach {
            if (isCancelNotify) {
                notifyHelper.notificationManager.cancel(it.notificationId)
            }
        }
    }

    fun deleteAll(isDeleteFile: Boolean, isCancelNotify: Boolean) {
        this.taskManager.deleteAll()
        val missionList = getDownloadTask()
        this.appDbHelper.deleteAllTasks()
                .compose(RxObservableTransformer.io_main())
                .compose(RxObservableTransformer.errorResult())
                .subscribe(object : RxSubscriber<Long>() {
                    override fun rxOnNext(t: Long) {
                        missionList.forEach {
                            if (isCancelNotify) {
                                notifyHelper.notificationManager.cancel(it.notificationId)
                            }
                            if (isDeleteFile) {
                                FsUtils.deleteFileOrDir(it.absolutePath)
                            }
                        }
                        missionList.forEach {
                            EventManager.post(it.apply {
                                this.downloadTaskStatusType = DownloadTaskStatusType.Delete
                            })
                        }
                        missionList.clear()
                        EventManager.post(TaskDeleteStatusEvent(TaskDeleteStatusEvent.Status.DELETE_ALL))
                    }

                    override fun rxOnError(e: Exception) = Unit
                })
    }

    fun delete(taskUrl: String, isDeleteFile: Boolean, isCancelNotify: Boolean) {
        val downloadTaskBean = getDownloadTask(taskUrl) ?: return
        this.downloadTaskLists.remove(downloadTaskBean)
        this.taskManager.delete(downloadTaskBean.url)
        this.appDbHelper.deleteSingleMission(downloadTaskBean)
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
                        EventManager.post(TaskDeleteStatusEvent(TaskDeleteStatusEvent.Status.DELETE_SINGLE, downloadTaskBean))
                        EventManager.post(downloadTaskBean.apply {
                            this.downloadTaskStatusType = DownloadTaskStatusType.Delete
                        })
                    }

                    override fun rxOnError(e: Exception) = Unit
                })
    }
}