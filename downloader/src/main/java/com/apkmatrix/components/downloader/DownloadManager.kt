package com.apkmatrix.components.downloader

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.IntRange
import com.apkmatrix.components.downloader.db.DownloadDatabase
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.misc.DownloadServiceInitCallback
import com.apkmatrix.components.downloader.misc.DownloadTaskUpdateDataCallback
import com.apkmatrix.components.downloader.misc.TaskConfig
import com.apkmatrix.components.downloader.misc.TaskManager
import com.apkmatrix.components.downloader.services.DownloadService14
import com.apkmatrix.components.downloader.services.DownloadService21
import com.apkmatrix.components.downloader.services.DownloadServiceAssistUtils
import com.apkmatrix.components.downloader.utils.CommonUtils
import com.apkmatrix.components.downloader.utils.DialogUtils
import com.liulishuo.okdownload.core.Util
import okhttp3.OkHttpClient
import com.liulishuo.okdownload.DownloadTask as OkDownloadTask

/**
 * author: mr.xiong
 * date: 2020/3/26
 */
object DownloadManager {
    var isInitDownloadServiceCompat = false
    var downloadServiceInitCallback: DownloadServiceInitCallback? = null
    var downloadTaskUpdateDataCallback: DownloadTaskUpdateDataCallback? = null

    fun initial(application: Application, builder: OkHttpClient.Builder, downloadServiceInitCallback: DownloadServiceInitCallback? = null) {
        this.downloadServiceInitCallback = downloadServiceInitCallback
        DownloadDatabase.initial(application)
        TaskManager.init(application, builder)
        this.startInitialTask(application)
    }

    fun setDebug(isDebug: Boolean) {
        TaskConfig.isDebug = isDebug
        if (isDebug) {
            Util.enableConsoleLog()
        }
    }

    fun startUpdateDownloadTaskData(mContext: Context, downloadTaskUpdateDataCallback: DownloadTaskUpdateDataCallback?) {
        this.downloadTaskUpdateDataCallback = downloadTaskUpdateDataCallback
        this.startUpdateDownloadTaskData(mContext)
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
            DownloadServiceAssistUtils.newInitIntent(mContext, DownloadService21::class.java).apply {
                DownloadService21.enqueueWorkService(mContext, this)
            }
        } else {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newInitIntent(mContext
                    , DownloadService14::class.java))
        }
    }

    private fun startUpdateDownloadTaskData(mContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newUpdateTaskDataIntent(mContext, DownloadService21::class.java).apply {
                DownloadService21.enqueueWorkService(mContext, this)
            }
        } else {
            CommonUtils.startService(mContext, DownloadServiceAssistUtils.newUpdateTaskDataIntent(mContext
                    , DownloadService14::class.java))
        }
    }

    fun startNewTask(mContext: Context, builder: DownloadTask.Builder, permissionSilent: Boolean = false, tipsSilent: Boolean = false) {
        if (DialogUtils.flowTipsDialog(mContext, tipsSilent) && DialogUtils.checkWriteExternalStorage(mContext, permissionSilent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newStartNewTaskIntent(mContext, DownloadService21::class.java, builder.build()).apply {
                    DownloadService21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStartNewTaskIntent(mContext,
                        DownloadService14::class.java, builder.build()))
            }
        }
    }

    fun stopTask(mContext: Context, id: String, silent: Boolean = false) {
        if (DialogUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newStopIntent(mContext, DownloadService21::class.java, id).apply {
                    DownloadService21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newStopIntent(mContext
                        , DownloadService14::class.java, id))
            }
        }
    }

    fun resumeTask(mContext: Context, id: String, silent: Boolean = false) {
        if (DialogUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newResumeIntent(mContext, DownloadService21::class.java, id).apply {
                    DownloadService21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newResumeIntent(mContext
                        , DownloadService14::class.java, id))
            }
        }
    }

    fun deleteTask(mContext: Context, ids: ArrayList<String>, isDeleteFile: Boolean = true, silent: Boolean = false) {
        if (DialogUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newDeleteIntent(mContext, DownloadService21::class.java, ids, isDeleteFile).apply {
                    DownloadService21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext
                        , DownloadService14::class.java, ids, isDeleteFile))
            }
        }
    }

    fun deleteAllTask(mContext: Context, silent: Boolean = false) {
        if (DialogUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newDeleteAllIntent(mContext, DownloadService21::class.java).apply {
                    DownloadService21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newDeleteAllIntent(mContext
                        , DownloadService14::class.java))
            }
        }
    }

    fun renameTaskFile(mContext: Context, id: String, fileName: String, silent: Boolean = false) {
        if (DialogUtils.checkWriteExternalStorage(mContext, silent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DownloadServiceAssistUtils.newRenameIntent(mContext, DownloadService21::class.java, id, fileName).apply {
                    DownloadService21.enqueueWorkService(mContext, this)
                }
            } else {
                CommonUtils.startService(mContext, DownloadServiceAssistUtils.newRenameIntent(mContext
                        , DownloadService14::class.java, id, fileName))
            }
        }
    }
}