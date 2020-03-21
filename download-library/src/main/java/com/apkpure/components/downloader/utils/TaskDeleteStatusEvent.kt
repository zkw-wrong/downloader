package com.apkpure.components.downloader.utils

import com.apkpure.components.downloader.db.bean.MissionDbBean

/**
 * @author xiongke
 * @date 2018/11/27
 */
class TaskDeleteStatusEvent(var status: Status, val missionDbBean: MissionDbBean? = null) {
    enum class Status {
        DELETE_ALL,
        DELETE_SINGLE
    }
}
