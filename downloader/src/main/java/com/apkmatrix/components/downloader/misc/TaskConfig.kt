package com.apkmatrix.components.downloader.misc

import android.graphics.Bitmap
import com.apkmatrix.components.downloader.utils.FsUtils
import java.io.File

/**
 * 下载配置
 * @author xiongke
 * @date 2018/12/13
 */
object TaskConfig {
    const val minIntervalMillisCallbackProcess = 300
    const val failedRetryCount = 3

    private var customDownloadDir: File? = null
    var notificationLargeIcon: Bitmap? = null
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
}