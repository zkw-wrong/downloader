package com.apkmatrix.components.downloader.db

import androidx.annotation.WorkerThread
import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
object AppDbHelper {
    //此处协程比如await等待其执行完毕
    @WorkerThread
    fun queryInitDownloadTask(): InitTask {
        val downloadTaskIngList = arrayListOf<DownloadTask>()
        val downloadTaskAllList = arrayListOf<DownloadTask>()
        val list = DownloadDatabase.instance.downloadTaskDao()
                .queryAllDownloadTask()
        downloadTaskAllList.addAll(list)
        downloadTaskAllList.forEach {
            if (it.downloadTaskStatus == DownloadTaskStatus.Downloading ||
                    it.downloadTaskStatus == DownloadTaskStatus.Waiting ||
                    it.downloadTaskStatus == DownloadTaskStatus.Preparing) {
                downloadTaskIngList.add(it)
                it.downloadTaskStatus = DownloadTaskStatus.Stop
            }
        }
        GlobalScope.async(Dispatchers.IO) {
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(downloadTaskAllList)
        }.onAwait
        return InitTask(downloadTaskAllList, downloadTaskIngList)
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