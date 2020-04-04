package com.apkpure.components.downloader.db.convert

import androidx.room.TypeConverter
import com.apkpure.components.downloader.db.Extras
import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import org.json.JSONObject
import java.util.*

/**
 * author: mr.xiong
 * date: 2020/4/3
 */
class Converters {
    @TypeConverter
    fun convertToEntityProperty1(databaseValue: String?): Extras? {
        return if (!databaseValue.isNullOrEmpty()) {
            val responseHeaders = mutableMapOf<String, String>()
            try {
                val json = JSONObject(databaseValue)
                json.keys().forEach {
                    responseHeaders[it] = json.get(it).toString()
                }
            } catch (e: Exception) {
            }
            Extras(responseHeaders)
        } else {
            null
        }
    }

    @TypeConverter
    fun convertToDatabaseValue1(entityProperty: Extras?): String? {
        return entityProperty?.toJSONString()
    }

    @TypeConverter
    fun convertToEntityProperty2(databaseValue: Int?): DownloadTaskStatus {
        return if (databaseValue != null) {
            DownloadTaskStatus.values()[databaseValue]
        } else {
            DownloadTaskStatus.Waiting
        }
    }

    @TypeConverter
    fun convertToDatabaseValue2(entityProperty: DownloadTaskStatus): Int {
        return entityProperty.typeId
    }

    @TypeConverter
    fun convertToEntityProperty3(databaseValue: Long): Date {
        return try {
            Date(databaseValue)
        } catch (e: Exception) {
            Date()
        }
    }

    @TypeConverter
    fun convertToDatabaseValue3(entityProperty: Date): Long {
        return entityProperty.time
    }

    @TypeConverter
    fun convertToEntityProperty4(databaseValue: Int): Boolean {
        return databaseValue == 0
    }

    @TypeConverter
    fun convertToDatabaseValue4(entityProperty: Boolean): Int {
        return if (entityProperty) {
            0
        } else {
            1
        }
    }
}