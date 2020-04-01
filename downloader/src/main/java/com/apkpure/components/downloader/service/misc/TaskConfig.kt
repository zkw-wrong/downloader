package com.apkpure.components.downloader.service.misc

import com.apkpure.components.downloader.utils.FsUtils
import java.io.File

/**
 * 下载配置
 * @author xiongke
 * @date 2018/12/13
 */
object TaskConfig {
    const val minIntervalMillisCallbackProcess = 300
    const val isWifiRequired = true
    const val isAutoCallbackToUIThread = true
    const val maxRunningCount = 5
    const val failedRetryCount = 3

    var customDownloadDir: File? = null

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