package com.apkpure.demo.download


import android.os.Environment
import com.apkpure.components.downloader.utils.FsUtils
import java.io.File

/**
 * @author Xiong Ke
 * @date 2018/4/12
 */

object AppFolder {
    private val APP_FOLDER_NAME: String
        get() {
            return "ApkPureDownload-Demo"
        }
    private const val APK_FOLDER_NAME = "apk"

    val apkFolder: File?
        get() = createAppFolderDirectory(APK_FOLDER_NAME)

    private val appFolder: File?
        get() {
            return if (FsUtils.isSdUsable) {
                val appFolder = File(Environment.getExternalStorageDirectory(), APP_FOLDER_NAME)
                FsUtils.createOnNotFound(appFolder)
            } else {
                null
            }
        }

    private fun createAppFolderDirectory(directoryName: String): File? {
        return FsUtils.createOnNotFound(File(appFolder, directoryName))
    }
}
