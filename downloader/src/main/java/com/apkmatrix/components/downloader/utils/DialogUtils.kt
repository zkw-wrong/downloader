package com.apkmatrix.components.downloader.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import com.apkmatrix.components.dialog.AlertDialogBuilder
import com.apkmatrix.components.dialog.HtmlAlertDialogBuilder
import com.apkmatrix.components.downloader.R
import java.io.File

/**
 * author: mr.xiong
 * date: 2020/6/21
 */
object DialogUtils {

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
        if (!externalStorageDirectory.canWrite() || !CommonUtils.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (!silent) {
                HtmlAlertDialogBuilder(mContext)
                        .setMessage(mContext.getString(R.string.q_external_storage_permission_denied))
                        .setPositiveButton(R.string.q_setting) { _, _ ->
                            val intent = Intent()
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            intent.data = Uri.fromParts(
                                    "package", mContext.applicationContext.packageName
                                    , null
                            )
                            mContext.startActivity(intent)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
            }
            return false
        }
        return true
    }

    fun flowTipsDialog(mContext: Context, tipsSilent: Boolean): Boolean {
        return !tipsSilent && if (NetWorkUtils.isMobile(mContext)) {
            HtmlAlertDialogBuilder(mContext)
                    .setTitle(R.string.q_download_over_cellular)
                    .setMessage(R.string.q_download_over_cellular_content)
                    .setPositiveButton(R.string.q_continue, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .showModal() == AlertDialogBuilder.RESULT_POSITIVE
        } else {
            true
        }
    }
}