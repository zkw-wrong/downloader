package com.apkpure.demo.download

import android.app.Application
import com.apkpure.components.downloader.service.DownloadManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * author: mr.xiong
 * date: 2020/3/19
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DownloadManager.initial(this, newOkHttpClientBuilder())
    }

    private fun newOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
    }
}