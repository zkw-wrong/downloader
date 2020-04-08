package com.apkpure.components.downloader.db

import android.content.Intent
import android.os.Parcelable
import androidx.room.*
import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
@Entity(tableName = "DownloadTaskTable", indices = [Index(value = ["_id"], unique = true)])
@Parcelize
class DownloadTask private constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Int,
        @ColumnInfo(name = "_url")
        var url: String,
        @ColumnInfo(name = "_absolute_path")
        var absolutePath: String,
        @ColumnInfo(name = "_download_task_status")
        var downloadTaskStatus: DownloadTaskStatus = DownloadTaskStatus.Waiting,
        @ColumnInfo(name = "_extras")
        var extras: Extras?,
        @ColumnInfo(name = "_date")
        var date: Date = Date(),
        @ColumnInfo(name = "_current_offset")
        var currentOffset: Long,
        @ColumnInfo(name = "_total_length")
        var totalLength: Long,
        @ColumnInfo(name = "_show_notification")
        var showNotification: Boolean,
        @ColumnInfo(name = "_notification_id")
        var notificationId: Int,
        @ColumnInfo(name = "_notification_title")
        var notificationTitle: String?,
        @Ignore
        var taskSpeed: String,
        @Ignore
        var overrideTask: Boolean,
        @ColumnInfo(name = "_headers")
        var headers: Extras?,
        @ColumnInfo(name = "_notification_intent")
        var notificationIntent: Intent?
) : Parcelable {
        private constructor() : this(
                0, String(), String(),
                DownloadTaskStatus.Waiting, null, Date(),
                0, 0, true,
                0, null, String(),
                false, null, null
        )

        inner class Builder
}