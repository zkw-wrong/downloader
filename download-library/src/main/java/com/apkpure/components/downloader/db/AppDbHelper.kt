package com.apkpure.components.downloader.db

import android.app.Application
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import com.apkpure.components.greendao.db.DaoMaster
import com.apkpure.components.greendao.db.DaoSession
import com.apkpure.components.greendao.db.MissionDbBeanDao
import io.reactivex.Observable

/**
 * @author Xiong Ke
 * @date 2017/12/1
 */
class AppDbHelper private constructor() : DbHelper {

    companion object {
        private var appDbHelper: AppDbHelper? = null
        private const val DATABASE_NAME = "download-library.db"
        private lateinit var mDaoSession: DaoSession
        private var isInitial = false

        val instance: AppDbHelper
            get() {
                if (appDbHelper == null) {
                    synchronized(AppDbHelper::class.java) {
                        if (appDbHelper == null) {
                            appDbHelper = AppDbHelper().apply {
                                if (!isInitial) {
                                    throw Exception("AppDbHelper not is initial!")
                                }
                            }
                        }
                    }
                }
                return appDbHelper!!
            }

        fun init(application: Application) {
            isInitial = true
            mDaoSession = DaoMaster(DaoMaster.DevOpenHelper(application, DATABASE_NAME).writableDb).newSession()
        }
    }

    override fun createOrUpdateMission(downloadTaskBean: DownloadTaskBean): Observable<Long> {
        return Observable.fromCallable {
            mDaoSession.missionDbBeanDao.apply {
                this.insertOrReplaceInTx(downloadTaskBean)
            }
            1L
        }
    }

    override fun queryAllMission(): Observable<List<DownloadTaskBean>> {
        return Observable.fromCallable {
            mDaoSession.missionDbBeanDao
                    .queryBuilder()
                    .orderDesc(MissionDbBeanDao.Properties.Date)
                    .build()
                    .list()
        }
    }

    override fun deleteSingleMission(downloadTaskBean: DownloadTaskBean): Observable<Long> {
        return Observable.fromCallable {
            mDaoSession.missionDbBeanDao.apply {
                this.delete(downloadTaskBean)
            }
            1L
        }
    }

    override fun deleteAllMission(): Observable<Long> {
        return Observable.fromCallable {
            mDaoSession.missionDbBeanDao.apply {
                this.deleteAll()
            }
            1L
        }
    }
}
