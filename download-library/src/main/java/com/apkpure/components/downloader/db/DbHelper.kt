package com.apkpure.components.downloader.db

import android.content.Context
import com.apkpure.components.downloader.db.bean.DownloadTask2
import com.apkpure.components.downloader.db.other.Helper
import com.apkpure.components.greendao.db.DaoMaster
import com.apkpure.components.greendao.db.DaoSession
import io.reactivex.Observable

/**
 * author: mr.xiong
 * date: 2020/3/19
 */
class DbHelper private constructor() : Helper {
    companion object {
        private const val DATABASE_NAME = "apk_pure_download"
        private var dbHelper: DbHelper? = null
        private lateinit var mDaoSession: DaoSession

        val instance: DbHelper
            get() {
                if (dbHelper == null) {
                    synchronized(DbHelper::class.java) {
                        if (dbHelper == null) {
                            dbHelper = DbHelper()
                        }
                    }
                }
                return dbHelper!!
            }

        fun init(mContext: Context) {
            mDaoSession = DaoMaster(DaoMaster.DevOpenHelper(mContext, DATABASE_NAME).writableDb)
                .newSession()
        }
    }

    override fun queryAllTask(): Observable<List<DownloadTask2>> {
        return Observable.fromCallable {
            mDaoSession.downloadTask2Dao.loadAll()
        }
    }

    override fun deleteAllTask(): Observable<Long> {
        return Observable.fromCallable {
            mDaoSession.downloadTask2Dao.deleteAll()
            1L
        }
    }

    override fun deleteTask(downloadTask2: DownloadTask2): Observable<Long> {
        return Observable.fromCallable {
            mDaoSession.downloadTask2Dao.delete(downloadTask2)
            1L
        }
    }
}