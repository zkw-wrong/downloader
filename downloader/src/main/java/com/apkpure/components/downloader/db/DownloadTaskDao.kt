package com.apkpure.components.downloader.db

import androidx.room.*

/**
 * author: mr.xiong
 * date: 2020/4/4
 */
@Dao
interface DownloadTaskDao {
    @Query("SELECT * FROM DownloadTaskTable")
    fun queryAllDownloadTask(): List<DownloadTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createOrUpdateDownloadTask(downloadTaskList: List<DownloadTask>)

    @Delete
    fun deleteTasks(downloadTaskList: List<DownloadTask>)
}