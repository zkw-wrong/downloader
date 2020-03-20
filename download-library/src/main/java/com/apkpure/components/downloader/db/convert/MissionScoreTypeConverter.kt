package com.apkpure.components.downloader.db.convert

import com.apkpure.components.downloader.db.enums.MissionScoreType
import org.greenrobot.greendao.converter.PropertyConverter

/**
 * @author xiongke
 * @date 2018/10/27
 */
class MissionScoreTypeConverter : PropertyConverter<MissionScoreType, Int> {
    override fun convertToEntityProperty(databaseValue: Int?): MissionScoreType {
        return if (databaseValue != null) {
            MissionScoreType.values()[databaseValue]
        } else {
            MissionScoreType.UNKNOWN
        }
    }

    override fun convertToDatabaseValue(entityProperty: MissionScoreType?): Int? {
        return entityProperty?.typeId
    }
}