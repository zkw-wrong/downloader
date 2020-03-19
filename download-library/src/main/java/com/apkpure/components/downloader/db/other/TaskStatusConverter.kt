package com.apkpure.components.downloader.db.other

import org.greenrobot.greendao.converter.PropertyConverter

/**
 * @author xiongke
 * @date 2018/10/27
 */
class TaskStatusConverter : PropertyConverter<TaskStatus, Int> {
    override fun convertToEntityProperty(databaseValue: Int?): TaskStatus {
        return if (databaseValue != null) {
            TaskStatus.values()[databaseValue]
        } else {
            TaskStatus.Waiting
        }
    }

    override fun convertToDatabaseValue(entityProperty: TaskStatus?): Int? {
        return entityProperty?.typeId
    }
}