package com.apkpure.demo.download

import android.app.Application
import com.apkpure.components.downloader.DownloadManager

/**
 * author: mr.xiong
 * date: 2020/3/19
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DownloadManager.initial(this)
    }
}