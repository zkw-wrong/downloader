package com.apkmatrix.components.downloader.misc

/**
 * @author xiongke
 * @date 2020/6/1
 */
interface DownloadServiceInitCallback {
    fun loadComplete()
}

interface DownloadTaskUpdateDataCallback {
    fun success()
    fun failed()
}