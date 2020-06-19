package com.apkmatrix.components.downloader.db

import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import kotlinx.coroutines.*

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
object AppDbHelper {
    fun queryInitDownloadTask(): InitTask {
        val downloadTaskIngList = arrayListOf<DownloadTask>()
        val downloadTaskList = arrayListOf<DownloadTask>()
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val list = DownloadDatabase.instance.downloadTaskDao()
                        .queryAllDownloadTask()
                downloadTaskList.addAll(list)
            }
            downloadTaskList.forEach {
                if (it.downloadTaskStatus == DownloadTaskStatus.Downloading ||
                        it.downloadTaskStatus == DownloadTaskStatus.Waiting ||
                        it.downloadTaskStatus == DownloadTaskStatus.Preparing) {
                    downloadTaskIngList.add(it)
                    it.downloadTaskStatus = DownloadTaskStatus.Stop
                }
            }
            async(Dispatchers.IO) {
                DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(downloadTaskList)
            }.onAwait
        }
        return InitTask(downloadTaskList, downloadTaskIngList)

    }

    fun createOrUpdateDownloadTask(downloadTask: DownloadTask): Long {
        GlobalScope.async(Dispatchers.IO) {
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(arrayListOf(downloadTask))
        }.onAwait
        return 1L
    }

    fun deleteTasks(downloadTasks: List<DownloadTask>): Long {
        GlobalScope.async(Dispatchers.IO) {
            DownloadDatabase.instance.downloadTaskDao().deleteTasks(downloadTasks)
        }.onAwait
        return 1L
    }

    fun deleteAllTasks(): Long {
        GlobalScope.async(Dispatchers.IO) {
            DownloadDatabase.instance.downloadTaskDao().apply {
                this.deleteTasks(this.queryAllDownloadTask())
            }
        }.onAwait
        return 1L
    }
}