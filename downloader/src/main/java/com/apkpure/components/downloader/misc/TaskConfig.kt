package com.apkpure.components.downloader.misc

import android.graphics.Bitmap
import com.apkpure.components.downloader.utils.FsUtils
import java.io.File
import java.lang.ref.SoftReference

/**
 * 下载配置
 * @author xiongke
 * @date 2018/12/13
 */
object TaskConfig {
    const val minIntervalMillisCallbackProcess = 300
    const val maxRunningCount = 5
    const val failedRetryCount = 3

    private var customDownloadDir: File? = null
    private var notificationLargeIcon: SoftReference<Bitmap>? = null
    var isDebug = false

    fun getOkDownloadAbsolutePath(fileName: String? = null): String {
        val downloadFileDir = if (FsUtils.exists(customDownloadDir)) {
            customDownloadDir!!
        } else {
            FsUtils.getDefaultDownloadDir()
        }
        return if (fileName.isNullOrEmpty()) {
            downloadFileDir.absolutePath
        } else {
            "${downloadFileDir.absolutePath}${File.separator}$fileName"
        }
    }

    fun setNotificationLargeIcon(bitmap: Bitmap) {
        notificationLargeIcon = SoftReference<Bitmap>(bitmap)
    }

    fun getNotificationLargeIcon() = notificationLargeIcon?.get()
}