package com.apkpure.components.downloader

import android.app.Application
import com.apkpure.components.downloader.db.DbHelper

/**
 * author: mr.xiong
 * date: 2020/3/18
 */
class DownloadManager {
    companion object {
        var downloadManager: DownloadManager? = null
        fun initial(application: Application) {
            DbHelper.init(application)
        }

        val instance: DownloadManager
            get() {
                if (downloadManager == null) {
                    synchronized(DownloadManager::class.java) {
                        if (downloadManager == null) {
                            downloadManager = DownloadManager()
                        }
                    }
                }
                return downloadManager!!
            }
    }
}