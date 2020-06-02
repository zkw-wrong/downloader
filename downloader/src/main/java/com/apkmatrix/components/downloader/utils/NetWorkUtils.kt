package com.apkmatrix.components.downloader.utils

import android.content.Context
import android.net.ConnectivityManager
import com.apkmatrix.components.dialog.AlertDialogBuilder
import com.apkmatrix.components.dialog.HtmlAlertDialogBuilder
import com.apkmatrix.components.downloader.R

/**
 * Created by Xiong Ke on 2017/8/16.
 */
object NetWorkUtils {
    fun isConnected(mContext: Context): Boolean {
        val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityManager?.activeNetworkInfo?.isConnected ?: false
    }

    fun isWifi(mContext: Context): Boolean {
        val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connectivityManager?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
    }

    fun flowTipsDialog(mContext: Context, tipsSilent: Boolean): Boolean {
        return !tipsSilent && if (!isWifi(mContext)) {
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
