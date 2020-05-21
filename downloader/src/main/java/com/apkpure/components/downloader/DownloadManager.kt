package com.apkpure.components.downloader

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.IntRange
import com.apkpure.components.downloader.db.DownloadDatabase
import com.apkpure.components.downloader.db.DownloadTask
import com.apkpure.components.downloader.misc.DownloadInitCallback
import com.apkpure.components.downloader.misc.TaskConfig
import com.apkpure.components.downloader.misc.TaskManager
import com.apkpure.components.downloader.services.DownloadService14
import com.apkpure.components.downloader.services.DownloadServiceAssistUtils
import com.apkpure.components.downloader.services.DownloadServiceV21
import com.apkpure.components.downloader.utils.CommonUtils
import com.apkpure.components.downloader.utils.NetWorkUtils
import com.apkpure.components.downloader.utils.PermissionUtils
import com.liulishuo.okdownload.core.Util
import okhttp3.OkHttpClient
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * author: mr.xiong
 * date: 2020/3/26
 */
object DownloadManager {
    var downloadInitCallback: DownloadInitCallback? = null

    fun initial(application: Application, builder: OkHttpClient.Builder, downloadInitCallback: DownloadInitCallback? = null) {
        this.downloadInitCallback = downloadInitCallback
        DownloadDatabase.initial(application)
        TaskManager.init(application, builder)
        startInitialTask(application)
    }

    fun setDebug(isDebug: Boolean) {
        TaskConfig.isDebug = isDebug
        if (isDebug) {
            Util.enableConsoleLog()
        }
    }

    fun setNotificationLargeIcon(bitmap: Bitmap) {
        TaskConfig.notificationLargeIcon = bitmap
    }

    fun setMaxParallelRunningCount(@IntRange(from = 1) maxRunningCount: Int) {
        TaskManager.instance.setMaxParallelRunningCount(maxRunningCount)
    }

    fun getDownloadTasks() = mutableListOf<DownloadTask>().apply {
        addAll(DownloadServiceAssistUtils.downloadTaskLists)
    }

    fun getDownloadTask(taskId: String): DownloadTask? {
        DownloadServiceAssistUtils.downloadTaskLists.iterator().forEach {
            if (it.id == taskId) {
                return it
            }
        }
        return null
    }

    fun getDownloadTask(okDownloadTask: OkDownloadTask): DownloadTask? {
        TaskManager.instance.getOkDownloadTaskId(okDownloadTask)?.let { it1 ->
            DownloadServiceAssistUtils.downloadTaskLists.iterator().forEach { it2 ->
                if (it1 == it2.id) {
                    return it2
                }
            }
        }
        return null
    }

    private fun startInitialTask(mContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newInitIntent(mContext, DownloadServiceV21::class.java).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newInitIntent(mContext
                    , DownloadService14::class.java))
        }
    }

    fun startNewTask(mContext: Context, builder: DownloadTask.Builder, permissionSilent: Boolean = false, tipsSilent: Boolean = false) {
        if (NetWorkUtils.flowTipsDialog(mContext, tipsSilent) && PermissionUtils.checkWriteExternalStorage(mContext, permissionSilent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newStartNewTaskIntent(mContext, DownloadServiceV21::class.java, builder.build()).apply {
                    DownloadServiceV21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStartNewTaskIntent(mContext,
                        DownloadService14::class.java, builder.build()))
            }
        }
    }

    fun stopTask(mContext: Context, id: String, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newStopIntent(mContext, DownloadServiceV21::class.java, id).apply {
                    DownloadServiceV21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStopIntent(mContext
                        , DownloadService14::class.java, id))
            }
        }
    }

    fun resumeTask(mContext: Context, id: String, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newResumeIntent(mContext, DownloadServiceV21::class.java, id).apply {
                    DownloadServiceV21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newResumeIntent(mContext
                        , DownloadService14::class.java, id))
            }
        }
    }

    fun deleteTask(mContext: Context, ids: ArrayList<String>, isDeleteFile: Boolean = true, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext
                    , DownloadService14::class.java, ids, isDeleteFile))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newDeleteIntent(mContext, DownloadServiceV21::class.java, ids, isDeleteFile).apply {
                    DownloadServiceV21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext
                        , DownloadService14::class.java, ids, isDeleteFile))
            }
        }
    }

    fun deleteAllTask(mContext: Context, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newDeleteAllIntent(mContext, DownloadServiceV21::class.java).apply {
                    DownloadServiceV21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteAllIntent(mContext
                        , DownloadService14::class.java))
            }
        }
    }

    fun renameTaskFile(mContext: Context, id: String, fileName: String, silent: Boolean = false) {
        if (PermissionUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newRenameIntent(mContext, DownloadServiceV21::class.java, id, fileName).apply {
                    DownloadServiceV21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newRenameIntent(mContext
                        , DownloadService14::class.java, id, fileName))
            }
        }
    }
}