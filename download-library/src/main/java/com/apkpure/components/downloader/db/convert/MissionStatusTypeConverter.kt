package com.apkpure.components.downloader.db.convert

import com.apkpure.components.downloader.db.enums.DownloadTaskStatusType
import org.greenrobot.greendao.converter.PropertyConverter

/**
 * @author xiongke
 * @date 2018/10/27
 */
class MissionStatusTypeConverter : PropertyConverter<DownloadTaskStatusType, Int> {
    override fun convertToEntityProperty(databaseValue: Int?): DownloadTaskStatusType {
        return if (databaseValue != null) {
            DownloadTaskStatusType.values()[databaseValue]
        } else {
            DownloadTaskStatusType.Waiting
        }
    }

    override fun convertToDatabaseValue(entityProperty: DownloadTaskStatusType?): Int? {
        return entityProperty?.typeId
    }
}