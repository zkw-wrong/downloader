package com.apkpure.components.downloader.misc

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