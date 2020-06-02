package com.apkmatrix.components.downloader.db

import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import io.reactivex.Observable

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
object AppDbHelper {
    fun queryInitDownloadTask(): Observable<InitTask> {
        return Observable.fromCallable {
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
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(downloadTaskList)
            InitTask(downloadTaskList, downloadTaskIngList)
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