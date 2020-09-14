package com.apkmatrix.components.downloader.utils

import android.os.Environment
import android.text.TextUtils
import java.io.File

/**
 * @author Xiong Ke
 * @date 2018/4/12
 */

object FsUtils {

    private val isSdUsable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun getStorageDir(): File? {
        return if (isSdUsable) {
            Environment.getExternalStorageDirectory()
        } else {
            null
        }
    }

    fun exists(filePath: String?): Boolean {
        return !TextUtils.isEmpty(filePath) && exists(File(filePath))
    }

    fun exists(file: File?): Boolean {
        return file != null && file.exists()
    }

    fun isFile(filePath: String?): Boolean {
        return if (!TextUtils.isEmpty(filePath)) {
            exists(filePath) && File(filePath).isFile
        } else {
            false
        }
    }

    fun deleteFileOrDir(filePath: String?) {
        filePath?.let {
            deleteFileOrDir(File(it))
        }
    }

    fun deleteFileOrDir(file: File?) {
        if (file != null && exists(file)) {
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                file.listFiles()?.forEach {
                    deleteFileOrDir(it)
                }
                file.delete()
            }
        }
    }

    fun createOnNotFound(folder: File?): File? {
        if (folder == null) {
            return null
        }
        if (!exists(folder)) {
            folder.mkdirs()
        }
        return if (exists(folder)) {
            folder
        } else {
            null
        }
    }

    fun getDefaultDownloadDir() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    fun renameFile(oldFile: File, fileName: String): File? {
        try {
            if (exists(oldFile) && fileName.isNotEmpty()) {
                val newFile = File(oldFile.parent, fileName)
                if (oldFile.renameTo(newFile)) {
                    return newFile
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
