package com.apkpure.components.downloader.db

import io.reactivex.Observable

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
object AppDbHelper {
    fun queryAllDownloadTask(): Observable<List<DownloadTaskBean>> {
        return Observable.fromCallable {
            DownloadDatabase.instance.downloadTaskDao().queryAllDownloadTask()
        }
    }

    fun createOrUpdateDownloadTask(downloadTaskBean: DownloadTaskBean): Observable<Long> {
        return Observable.fromCallable {
            DownloadDatabase.instance.downloadTaskDao().createOrUpdateDownloadTask(downloadTaskBean)
            1L
        }
    }

    fun deleteTasks(downloadTasks: List<DownloadTaskBean>): Observable<Long> {
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