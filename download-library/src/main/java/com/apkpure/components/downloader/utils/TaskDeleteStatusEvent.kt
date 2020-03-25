package com.apkpure.components.downloader.utils

import com.apkpure.components.downloader.db.bean.DownloadTaskBean

/**
 * @author xiongke
 * @date 2018/11/27
 */
class TaskDeleteStatusEvent(var status: Status, val downloadTaskBean: DownloadTaskBean? = null) {
    enum class Status {
        DELETE_ALL,
        DELETE_SINGLE
    }
}
