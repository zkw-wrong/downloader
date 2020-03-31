package com.apkpure.components.downloader.utils

import android.os.Parcelable
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import kotlinx.android.parcel.Parcelize

/**
 * @author xiongke
 * @date 2018/11/27
 */
@Parcelize
class TaskDeleteStatusEvent(var status: Status, val downloadTaskBean: DownloadTaskBean? = null) : Parcelable {
    enum class Status {
        DELETE_ALL,
        DELETE_SINGLE
    }
}
