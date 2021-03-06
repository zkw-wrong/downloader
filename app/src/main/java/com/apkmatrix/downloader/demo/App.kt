package com.apkmatrix.downloader.demo

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import com.apkmatrix.components.downloader.DownloadManager
import com.apkmatrix.components.downloader.misc.DownloadServiceInitCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * author: mr.xiong
 * date: 2020/3/19
 */
class App : Application() {
    private lateinit var mContext: Context

    override fun onCreate() {
        super.onCreate()
        mContext = this
        DownloadManager.initial(this, newOkHttpClientBuilder(), object : DownloadServiceInitCallback {
            override fun loadComplete() {
                Toast.makeText(this@App, "loadCompat", Toast.LENGTH_LONG).show()
            }
        })
        DownloadManager.setDebug(true)
        GlobalScope.async(Dispatchers.IO) {
            getAppIcon(mContext.packageManager, mContext.applicationInfo)?.let {
                DownloadManager.setNotificationLargeIcon(it)
            }
        }.onAwait
    }

    private fun newOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
    }

    private fun getAppIcon(packageManager: PackageManager, applicationInfo: ApplicationInfo): Bitmap? {
        try {
            packageManager.getApplicationIcon(applicationInfo).apply {
                if (this is BitmapDrawable) {
                    return this.bitmap
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this is AdaptiveIconDrawable) {
                    val newBitmap = Bitmap.createBitmap(this.intrinsicWidth,
                            this.intrinsicHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(newBitmap)
                    this.setBounds(0, 0, canvas.width, canvas.height)
                    this.draw(canvas)
                    return newBitmap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}