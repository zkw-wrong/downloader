package com.apkpure.components.downloader.db

import android.content.Intent
import android.os.Parcelable
import androidx.room.*
import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import com.apkpure.components.downloader.service.misc.TaskConfig
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
@Entity(tableName = "DownloadTaskTable", indices = [Index(value = ["_id"], unique = true)])
@Parcelize
class DownloadTask private constructor(
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
        var notificationTitle: String?,
        @Ignore
        var taskSpeed: String,
        @Ignore
        var overrideTaskFile: Boolean,
        @ColumnInfo(name = "_headers")
        var headers: Extras?,
        @ColumnInfo(name = "_notification_intent")
        var notificationIntent: Intent?,
        @ColumnInfo(name = "_notification_id")
        var notificationId: Int
) : Parcelable {
    private constructor() : this(
            String(), String(), String(),
            DownloadTaskStatus.Waiting, null, Date(),
            0, 0, true,
            null, String(), false,
            null, null, 0
    )

    class Builder {
        private val downloadTask by lazy {
            DownloadTask().apply {
                this.showNotification = true
            }
        }

        private var fileName: String? = null

        fun setUrl(url: String): Builder {
            this.downloadTask.url = url
            return this
        }

        fun setExtras(extras: Extras): Builder {
            this.downloadTask.extras = extras
            return this
        }

        fun setFileName(fileName: String): Builder {
            this.fileName = fileName
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

        fun build(): DownloadTask {
            return this.downloadTask.apply {
                if (!fileName.isNullOrEmpty()) {
                    TaskConfig.getOkDownloadAbsolutePath(fileName)
                }
            }
        }
    }
}