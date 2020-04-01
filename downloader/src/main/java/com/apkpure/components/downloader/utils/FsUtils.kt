package com.apkpure.components.downloader.utils

import android.os.Environment
import android.text.TextUtils
import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileFilter
import java.lang.Exception
import java.util.*

/**
 * @author Xiong Ke
 * @date 2018/4/12
 */

object FsUtils {

    val isSdUsable: Boolean
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
        val file = File(filePath)
        return exists(file) && file.isFile
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

    //获取文件夹下所有的文件
    @WorkerThread
    fun getDirFilesArray(file: File?, fileFilter: FileFilter? = null): ArrayList<File> {
        val listFile = ArrayList<File>()
        file?.apply {
            if (this.isFile) {
                if (fileFilter != null) {
                    if (fileFilter.accept(this)) {
                        listFile.add(this)
                    }
                } else {
                    listFile.add(this)
                }
            } else if (this.isDirectory) {
                this.listFiles()?.forEach {
                    listFile.addAll(getDirFilesArray(it, fileFilter))
                }
            }
        }
        return listFile
    }

    fun getFileOrDirLength(filePath: String?): Long {
        return if (!TextUtils.isEmpty(filePath)) {
            this.getFileOrDirLength(File(filePath))
        } else {
            0L
        }
    }

    fun getFileOrDirLength(dirFile: File?): Long {
        var length = 0L
        if (dirFile != null && exists(dirFile)) {
            if (dirFile.isFile) {
                length += dirFile.length()
            } else if (dirFile.isDirectory) {
                getDirFilesArray(dirFile).forEach {
                    length += getFileOrDirLength(it)
                }
            }
        }
        return length
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
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }
}
