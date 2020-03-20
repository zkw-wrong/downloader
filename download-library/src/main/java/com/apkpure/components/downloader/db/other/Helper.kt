package com.apkpure.components.downloader.db.other

import com.apkpure.components.downloader.db.bean.DownloadTask2
import io.reactivex.Observable


/**
 * author: mr.xiong
 * date: 2020/3/19
 */
interface Helper {
    fun queryAllTask(): Observable<List<DownloadTask2>>

    fun deleteAllTask(): Observable<Long>

    fun deleteTask(downloadTask2: DownloadTask2): Observable<Long>
}