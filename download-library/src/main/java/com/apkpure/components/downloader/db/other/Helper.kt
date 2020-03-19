package com.apkpure.components.downloader.db.other

import com.apkpure.components.downloader.db.bean.DownloadTask
import io.reactivex.Observable


/**
 * author: mr.xiong
 * date: 2020/3/19
 */
interface Helper {
    fun queryAllTask(): Observable<List<DownloadTask>>
}