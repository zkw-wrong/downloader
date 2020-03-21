package com.apkpure.components.downloader.service.misc

/**
 * 下载配置
 * @author xiongke
 * @date 2018/12/13
 */
object TaskConfig {
    const val minIntervalMillisCallbackProcess = 300
    const val isWifiRequired = true
    const val isAutoCallbackToUIThread = true
    const val maxRunningCount = 3
    const val failedRetryCount = 3
}