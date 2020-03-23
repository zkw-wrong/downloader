package com.apkpure.components.downloader.service.misc

import android.app.Application
import android.os.Build
import com.apkpure.components.downloader.db.AppDbHelper
import com.apkpure.components.downloader.db.bean.MissionDbBean
import com.apkpure.components.downloader.db.enums.MissionStatusType
import com.apkpure.components.downloader.utils.EventManager
import com.apkpure.components.downloader.utils.FsUtils
import com.apkpure.components.downloader.utils.NotifyHelper
import com.apkpure.components.downloader.utils.TaskDeleteStatusEvent
import com.apkpure.components.downloader.utils.rx.RxObservableTransformer
import com.apkpure.components.downloader.utils.rx.RxSubscriber

/**
 * @author xiongke
 * @date 2018/11/16
 */
class DownloadManager {
    private val appDbHelper by lazy { AppDbHelper.instance }
    private val notifyHelper by lazy { NotifyHelper(application) }
    private val missionDbBeanList by lazy { mutableListOf<MissionDbBean>() }
    private val taskManager by lazy { TaskManager.getInstance() }
    private val downloadListener by lazy { DownloadListener() }
    private var taskListener: DownloadListener.TaskListener? = null

    companion object {
        private var downloadManager: DownloadManager? = null
        private lateinit var application: Application

        fun initial(application: Application) {
            this.application = application
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               // KeepAliveJobService.startJob(application)
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
        this.appDbHelper.queryAllMission()
            .compose(RxObservableTransformer.io_main())
            .compose(RxObservableTransformer.errorResult())
            .subscribe(object : RxSubscriber<List<MissionDbBean>>() {
                override fun rxOnNext(t: List<MissionDbBean>) {
                    missionDbBeanList.apply {
                        this.clear()
                        this.addAll(t)
                    }
                }

                override fun rxOnError(e: Exception) = Unit
            })
        this.taskManager.setDownloadListener(downloadListener)
    }

    fun setTaskListener(taskListener: DownloadListener.TaskListener) {
        this.taskListener = taskListener
        this.downloadListener.setTaskListener(taskListener)
    }

    fun start(missionDbBean: MissionDbBean) {
        if (getMissionTask(missionDbBean.url) == null) {
            missionDbBeanList.add(0, missionDbBean)
        }
        taskManager.start(missionDbBean.url, missionDbBean.absolutePath)
    }

    fun getCompletedDownloadTask(): MutableList<MissionDbBean> {
        val completedPosts: MutableList<MissionDbBean> = mutableListOf()
        for (index in 0 until missionDbBeanList.size) {
            val missionDbBean = missionDbBeanList[index]
            if (missionDbBean.missionStatusType == MissionStatusType.Success) {
                completedPosts.add(missionDbBean)
            }
        }
        return completedPosts
    }

    fun getMissionTask(taskUrl: String): MissionDbBean? {
        var missionDbBean: MissionDbBean? = null
        missionDbBeanList.iterator().forEach {
            if (it.url == taskUrl) {
                missionDbBean = it
            }
        }
        return missionDbBean
    }

    fun getDownloadTask() = this.missionDbBeanList

    fun getNotCompatDownloadTask() = this.missionDbBeanList.filter {
        it.missionStatusType == MissionStatusType.Waiting
                || it.missionStatusType == MissionStatusType.Preparing
                || it.missionStatusType == MissionStatusType.Downloading
    }

    fun startAll() {
        this.taskManager.startOnParallel()
    }

    fun stop(missionDbBean: MissionDbBean, isCancelNotify: Boolean) {
        this.taskManager.stop(missionDbBean.url)
        if (isCancelNotify) {
            notifyHelper.notificationManager.cancel(missionDbBean.notificationId)
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
        this.appDbHelper.deleteAllMission()
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
                            this.missionStatusType = MissionStatusType.Delete
                        })
                    }
                    missionList.clear()
                    EventManager.post(TaskDeleteStatusEvent(TaskDeleteStatusEvent.Status.DELETE_ALL))
                }

                override fun rxOnError(e: Exception) = Unit
            })
    }

    fun delete(missionDbBean: MissionDbBean, isDeleteFile: Boolean, isCancelNotify: Boolean) {
        this.missionDbBeanList.remove(missionDbBean)
        this.taskManager.delete(missionDbBean.url)
        this.appDbHelper.deleteSingleMission(missionDbBean)
            .compose(RxObservableTransformer.io_main())
            .compose(RxObservableTransformer.errorResult())
            .subscribe(object : RxSubscriber<Long>() {
                override fun rxOnNext(t: Long) {
                    if (isDeleteFile) {
                        FsUtils.deleteFileOrDir(missionDbBean.absolutePath)
                    }
                    if (isCancelNotify) {
                        notifyHelper.notificationManager.cancel(missionDbBean.notificationId)
                    }
                    EventManager.post(
                        TaskDeleteStatusEvent(
                            TaskDeleteStatusEvent.Status.DELETE_SINGLE,
                            missionDbBean
                        )
                    )
                    EventManager.post(missionDbBean.apply {
                        this.missionStatusType = MissionStatusType.Delete
                    })
                }

                override fun rxOnError(e: Exception) = Unit
            })
    }
}