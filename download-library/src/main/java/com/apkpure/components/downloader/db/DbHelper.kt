package com.apkpure.components.downloader.db


import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import io.reactivex.Observable


/**
 * @author Xiong Ke
 * @date 2017/12/4
 */

internal interface DbHelper {

    fun createOrUpdateDownloadTask(downloadTaskBean: DownloadTaskBean): Observable<Long>

    fun queryAllDownloadTask(): Observable<List<DownloadTaskBean>>

    fun deleteSingleMission(downloadTaskBean: DownloadTaskBean): Observable<Long>

    fun deleteAllTasks(): Observable<Long>
}
