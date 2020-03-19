package com.apkpure.components.downloader.db

import android.content.Context
import com.apkpure.components.downloader.db.bean.DownloadTask
import com.apkpure.components.greendao.db.DaoMaster
import com.apkpure.components.greendao.db.DaoSession
import io.reactivex.Observable

/**
 * author: mr.xiong
 * date: 2020/3/19
 */
class DbHelper private constructor() :
    Helper {
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

    override fun queryAllTask(): Observable<List<DownloadTask>> {
        return Observable.fromCallable {
            mDaoSession.downloadTaskDao.loadAll()
        }
    }
}