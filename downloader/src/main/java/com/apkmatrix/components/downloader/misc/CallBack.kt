package com.apkmatrix.components.downloader.misc

import com.apkmatrix.components.downloader.db.DownloadTask
import kotlinx.coroutines.CancellableContinuation

/**
 * @author xiongke
 * @date 2020/6/1
 */
interface DownloadServiceInitCallback {
    fun loadCompat()
}

interface DownloadTaskUpdateDataCallback {
    fun success()
    fun failed()
}

interface InitDataCallBack {
    fun success(list: List<DownloadTask>)
    fun failed()
}

interface DownloadPermission {
    fun requestPermission(cancellableContinuation: CancellableContinuation<Any>)
}