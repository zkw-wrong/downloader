package com.apkpure.components.downloader.db

import android.app.Application
import com.apkpure.components.downloader.db.bean.MissionDbBean
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
        private const val DATABASE_NAME = "download-library"
        private lateinit var mDaoSession: DaoSession

        val instance: AppDbHelper
            get() {
                if (appDbHelper == null) {
                    synchronized(AppDbHelper::class.java) {
                        if (appDbHelper == null) {
                            appDbHelper = AppDbHelper()
                        }
                    }
                }
                return appDbHelper!!
            }

        fun init(application: Application) {
            mDaoSession = DaoMaster(DaoMaster.DevOpenHelper(application, DATABASE_NAME).writableDb).newSession()
        }
    }

    override fun createOrUpdateMission(missionDbBean: MissionDbBean): Observable<Long> {
        return Observable.fromCallable {
            mDaoSession.missionDbBeanDao.apply {
                this.insertOrReplaceInTx(missionDbBean)
            }
            1L
        }
    }

    override fun queryAllMission(): Observable<List<MissionDbBean>> {
        return Observable.fromCallable {
            mDaoSession.missionDbBeanDao
                .queryBuilder()
                .orderDesc(MissionDbBeanDao.Properties.Date)
                .build()
                .list()
        }
    }

    override fun deleteSingleMission(missionDbBean: MissionDbBean): Observable<Long> {
        return Observable.fromCallable {
            mDaoSession.missionDbBeanDao.apply {
                this.delete(missionDbBean)
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
