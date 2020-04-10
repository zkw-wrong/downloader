package com.apkpure.components.downloader.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apkpure.components.downloader.db.convert.Converters

/**
 * author: mr.xiong
 * date: 2020/4/3
 */
@Database(entities = [DownloadTask::class], version = 6)
@TypeConverters(Converters::class)
abstract class DownloadDatabase : RoomDatabase() {
    abstract fun downloadTaskDao(): DownloadTaskDao

    companion object {
        private lateinit var application: Application
        private const val DB_NAME = "downloader_library.db"
        private var appDatabase: DownloadDatabase? = null
        fun initial(application: Application) {
            this.application = application
            appDatabase = Room.databaseBuilder(application, DownloadDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                .build()
        }

        val instance: DownloadDatabase
            get() {
                if (appDatabase == null) {
                    synchronized(DownloadDatabase::class.java) {
                        if (appDatabase == null) {
                            initial(application)
                        }
                    }
                }
                return appDatabase!!
            }
    }
}