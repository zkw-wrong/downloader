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
        return try {
            val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.activeNetworkInfo?.isConnected ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun isWifi(mContext: Context): Boolean {
        return try {
            val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val networkInfo = connectivityManager?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
        } catch (e: Exception) {
            false
        }
    }

    fun isMobile(mContext: Context): Boolean {
        return try {
            val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val networkInfo = connectivityManager?.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
        } catch (e: Exception) {
            false
        }
    }

    fun flowTipsDialog(mContext: Context, tipsSilent: Boolean): Boolean {
        return !tipsSilent && if (isMobile(mContext)) {
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
