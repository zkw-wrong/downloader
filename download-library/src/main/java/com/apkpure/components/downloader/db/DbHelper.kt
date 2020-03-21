package com.apkpure.components.downloader.db


import com.apkpure.components.downloader.db.bean.MissionDbBean
import io.reactivex.Observable


/**
 * @author Xiong Ke
 * @date 2017/12/4
 */

internal interface DbHelper {

    fun createOrUpdateMission(missionDbBean: MissionDbBean): Observable<Long>

    fun queryAllMission(): Observable<List<MissionDbBean>>

    fun deleteSingleMission(missionDbBean: MissionDbBean): Observable<Long>

    fun deleteAllMission(): Observable<Long>
}
