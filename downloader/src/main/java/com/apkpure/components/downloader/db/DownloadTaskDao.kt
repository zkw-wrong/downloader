package com.apkpure.components.downloader.db

import androidx.room.*

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
@Dao
interface DownloadTaskDao {
    @Query("SELECT * FROM DownloadTaskTable")
    fun queryAllDownloadTask(): List<DownloadTaskBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createOrUpdateDownloadTask(downloadTaskBean: DownloadTaskBean)

    @Delete
    fun deleteTasks(downloadTaskList: List<DownloadTaskBean>)
}