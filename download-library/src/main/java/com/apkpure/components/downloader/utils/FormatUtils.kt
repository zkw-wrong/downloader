package com.apkpure.components.downloader.utils

import java.text.DecimalFormat

/**
 * @author xiongke
 * @date 2018/9/20
 */
object FormatUtils {
    fun formatFileLength(sizeBytes: Long): String {
        if (sizeBytes <= 0) {
            return "0B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "EB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#.##").format(sizeBytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun formatPercent(progress: Long, count: Long): Int {
        return (progress * 1f / count * 100f).toInt()
    }

    fun formatPercentInfo(progress: Long, count: Long): String {
        return DecimalFormat("##%").format(progress.toDouble() / count.toDouble())
    }
}
