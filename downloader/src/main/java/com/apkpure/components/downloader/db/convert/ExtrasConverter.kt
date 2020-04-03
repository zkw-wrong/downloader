package com.apkpure.components.downloader.db.convert

import com.apkpure.components.downloader.db.Extras
import org.greenrobot.greendao.converter.PropertyConverter
import org.json.JSONObject

/**
 * @author xiongke
 * @date 2018/10/27
 */
class ExtrasConverter : PropertyConverter<Extras, String> {

    override fun convertToEntityProperty(databaseValue: String?): Extras? {
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

    override fun convertToDatabaseValue(entityProperty: Extras?): String {
        return entityProperty?.toJSONString() ?: String()
    }
}