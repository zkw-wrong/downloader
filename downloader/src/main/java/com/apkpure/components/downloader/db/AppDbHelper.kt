package com.apkpure.components.downloader.db

import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import io.reactivex.Observable

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
object AppDbHelper {
    fun queryInitAllDownloadTask(): Observable<List<DownloadTask>> {
        return Observable.fromCallable {
            val downloadTaskList = DownloadDatabase.instance.downloadTaskDao().queryAllDownloadTask()
            downloadTaskList.forEach {
                if (it.downloadTaskStatus == DownloadTaskStatus.Downloading) {
                    it.downloadTaskStatus = DownloadTaskStatus.Stop
                }
            }
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(downloadTaskList)
            downloadTaskList
        }
    }

    fun queryAllDownloadTask(): Observable<List<DownloadTask>> {
        return Observable.fromCallable {
            DownloadDatabase.instance.downloadTaskDao().queryAllDownloadTask()
        }
    }

    fun createOrUpdateDownloadTask(downloadTask: DownloadTask): Observable<Long> {
        return Observable.fromCallable {
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(arrayListOf(downloadTask))
            1L
        }
    }

    fun deleteTasks(downloadTasks: List<DownloadTask>): Observable<Long> {
        return Observable.fromCallable {
            DownloadDatabase.instance.downloadTaskDao().deleteTasks(downloadTasks)
            1L
        }
    }

    fun deleteAllTasks(): Observable<Long> {
        return Observable.fromCallable {
            DownloadDatabase.instance.downloadTaskDao().apply {
                this.deleteTasks(this.queryAllDownloadTask())
            }
            1L
        }
    }
}