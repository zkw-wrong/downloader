package com.apkmatrix.components.downloader.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.annotation.CheckResult
import androidx.core.content.ContextCompat
import com.apkmatrix.components.dialog.HtmlAlertDialogBuilder
import com.apkmatrix.components.downloader.R
import java.io.File

/**
 * author: mr.xiong
 * date: 2020/4/2
 */
object PermissionUtils {
    @CheckResult
    fun checkSelfPermission(mContext: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun checkWriteExternalStorage(mContext: Context, silent: Boolean): Boolean {
        val externalStorageState: String = Environment.getExternalStorageState()
        val externalStorageDirectory: File = Environment.getExternalStorageDirectory()
        if (Environment.MEDIA_MOUNTED != externalStorageState) {
            if (!silent) {
                HtmlAlertDialogBuilder(mContext)
                        .setMessage(mContext.getString(R.string.q_external_storage_not_usable))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
            return false
        }
        if (!externalStorageDirectory.canWrite() || !checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (!silent) {
                HtmlAlertDialogBuilder(mContext)
                        .setMessage(mContext.getString(R.string.q_external_storage_permission_denied))
                        .setPositiveButton(R.string.q_setting) { _, _ ->
                            val intent = Intent()
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            intent.data = Uri.fromParts("package", mContext.applicationContext.packageName
                                    , null)
                            mContext.startActivity(intent)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
            }
            return false
        }
        return true
    }
}