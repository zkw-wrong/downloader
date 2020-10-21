package com.apkmatrix.components.downloader.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.apkmatrix.components.dialog.AlertDialogBuilder
import com.apkmatrix.components.dialog.HtmlAlertDialogBuilder
import com.apkmatrix.components.downloader.R

/**
 * author: mr.xiong
 * date: 2020/6/21
 */
object DialogUtils {
    fun checkSdUsable(mContext: Context, silent: Boolean): Boolean {
        if (!FsUtils.isSdUsable) {
            if (!silent) {
                HtmlAlertDialogBuilder(mContext)
                        .setMessage(mContext.getString(R.string.q_external_storage_not_usable))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
            return false
        }
        return true
    }

    private fun checkSelfStoragePermission(mContext: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    fun checkExternalStorageUsable(mContext: Context, silent: Boolean): Boolean {
        if (!checkSelfStoragePermission(mContext)) {
            if (!silent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HtmlAlertDialogBuilder(mContext)
                            .setTitle(mContext.getString(R.string.q_hint))
                            .setMessage(mContext.getString(R.string.base_dialog_get_all_file_access_content))
                            .setMessageTextViewSize(mContext.resources.getDimension(R.dimen.base_dialog_massage_size))
                            .setMessageTextViewColor(R.color.base_dialog_message_color)
                            .setPositiveButton(R.string.q_setting) { _, _ ->
                                mContext.startActivity(CommonUtils.getSettingAllFilesPermission(mContext))
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .setCancelable(true)
                            .setCanceledOnTouchOutside(true)
                            .show()
                } else {
                    HtmlAlertDialogBuilder(mContext)
                            .setTitle(mContext.getString(R.string.q_hint))
                            .setMessage(mContext.getString(R.string.q_external_storage_permission_denied))
                            .setMessageTextViewSize(mContext.resources.getDimension(R.dimen.base_dialog_massage_size))
                            .setMessageTextViewColor(R.color.base_dialog_message_color)
                            .setPositiveButton(R.string.q_setting) { _, _ ->
                                mContext.startActivity(CommonUtils.getDetailSetting(mContext))
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .setCancelable(true)
                            .setCanceledOnTouchOutside(true)
                            .show()
                }
            }
            return false
        }
        return true
    }

    fun mobileNetworkDialog(mContext: Context, tipsSilent: Boolean): Boolean {
        return when {
            tipsSilent -> {
                true
            }
            NetWorkUtils.isMobile(mContext) -> {
                HtmlAlertDialogBuilder(mContext)
                        .setTitle(R.string.q_download_over_cellular)
                        .setMessage(R.string.q_download_over_cellular_content)
                        .setPositiveButton(R.string.q_continue, null)
                        .setNegativeButton(android.R.string.cancel, null)
                        .showModal() == AlertDialogBuilder.RESULT_POSITIVE
            }
            else -> {
                true
            }
        }
    }
}