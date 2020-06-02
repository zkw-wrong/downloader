package com.apkmatrix.components.downloader.db

import android.content.Intent
import android.os.Parcelable
import androidx.room.*
import com.apkmatrix.components.downloader.db.enums.DownloadTaskStatus
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
@Entity(tableName = "DownloadTaskTable", indices = [Index(value = ["_id"], unique = true)])
@Parcelize
class DownloadTask constructor(
        @PrimaryKey
        @ColumnInfo(name = "_id")
        var id: String,
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
        @ColumnInfo(name = "_notification_title")
        var notificationTitle: String,
        @Ignore
        var taskSpeed: String,
        @Ignore
        var overrideTaskFile: Boolean,
        @ColumnInfo(name = "_headers")
        var headers: Extras?,
        @ColumnInfo(name = "_notification_intent")
        var notificationIntent: Intent?,
        @ColumnInfo(name = "_notification_id")
        var notificationId: Int,
        @ColumnInfo(name = "_temp_fileName")
        var tempFileName: String
) : Parcelable {
    constructor() : this(
            String(), String(), String(),
            DownloadTaskStatus.Waiting, null, Date(),
            0, 0, true,
            String(), String(), true,
            null, null, 0,
            String()
    )

    class Builder {
        private val downloadTask by lazy {
            DownloadTask().apply {
                this.showNotification = true
                this.overrideTaskFile = true
            }
        }

        fun setUrl(url: String): Builder {
            this.downloadTask.url = url
            return this
        }

        fun setExtras(extras: Extras): Builder {
            this.downloadTask.extras = extras
            return this
        }

        fun setFileName(fileName: String): Builder {
            this.downloadTask.tempFileName = fileName
            return this
        }

        fun setShowNotification(showNotification: Boolean): Builder {
            this.downloadTask.showNotification = showNotification
            return this
        }

        fun setNotificationTitle(notificationTitle: String): Builder {
            this.downloadTask.notificationTitle = notificationTitle
            return this
        }

        fun setOverrideTaskFile(overrideTaskFile: Boolean): Builder {
            this.downloadTask.overrideTaskFile = overrideTaskFile
            return this
        }

        fun setHeaders(headers: Extras): Builder {
            this.downloadTask.headers = headers
            return this
        }

        fun setNotificationIntent(notificationIntent: Intent): Builder {
            this.downloadTask.notificationIntent = notificationIntent
            return this
        }

        fun setAbsolutePath(absolutePath: String): Builder {
            this.downloadTask.absolutePath = absolutePath
            return this
        }

        fun build(): DownloadTask {
            return this.downloadTask
        }
    }
}