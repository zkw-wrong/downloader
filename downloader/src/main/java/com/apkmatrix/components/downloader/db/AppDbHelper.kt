package com.apkmatrix.components.downloader.db

import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import com.apkmatrix.components.downloader.utils.ThreadUtils

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
object AppDbHelper {
    fun queryInitDownloadTask(): InitTask {
        val downloadTaskList = DownloadDatabase.instance.downloadTaskDao().queryAllDownloadTask()
        val downloadTaskIngList = arrayListOf<DownloadTask>()
        downloadTaskList.forEach {
            if (it.downloadTaskStatus == DownloadTaskStatus.Downloading ||
                    it.downloadTaskStatus == DownloadTaskStatus.Waiting ||
                    it.downloadTaskStatus == DownloadTaskStatus.Preparing) {
                downloadTaskIngList.add(it)
                it.downloadTaskStatus = DownloadTaskStatus.Stop
            }
        }
        ThreadUtils.runThreadPollProxy(Runnable {
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(downloadTaskList)
        })
        return InitTask(downloadTaskList, downloadTaskIngList)

    }

    fun createOrUpdateDownloadTask(downloadTask: DownloadTask): Long {
        ThreadUtils.runThreadPollProxy(Runnable {
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(arrayListOf(downloadTask))
        })
        return 1L
    }

    fun deleteTasks(downloadTasks: List<DownloadTask>): Long {
        ThreadUtils.runThreadPollProxy(Runnable {
            DownloadDatabase.instance.downloadTaskDao().deleteTasks(downloadTasks)
        })
        return 1L
    }

    fun deleteAllTasks(): Long {
        ThreadUtils.runThreadPollProxy(Runnable {
            DownloadDatabase.instance.downloadTaskDao().apply {
                this.deleteTasks(this.queryAllDownloadTask())
            }
        })
        return 1L
    }
}