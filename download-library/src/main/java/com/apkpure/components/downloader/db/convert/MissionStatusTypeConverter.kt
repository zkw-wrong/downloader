package com.apkpure.components.downloader.db.convert

import com.apkpure.components.downloader.db.enums.MissionStatusType
import org.greenrobot.greendao.converter.PropertyConverter

/**
 * @author xiongke
 * @date 2018/10/27
 */
class MissionStatusTypeConverter : PropertyConverter<MissionStatusType, Int> {
    override fun convertToEntityProperty(databaseValue: Int?): MissionStatusType {
        return if (databaseValue != null) {
            MissionStatusType.values()[databaseValue]
        } else {
            MissionStatusType.Waiting
        }
    }

    override fun convertToDatabaseValue(entityProperty: MissionStatusType?): Int? {
        return entityProperty?.typeId
    }
}