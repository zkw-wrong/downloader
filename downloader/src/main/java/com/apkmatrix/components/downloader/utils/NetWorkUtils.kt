package com.apkmatrix.components.downloader.utils

import android.content.Context
import android.net.ConnectivityManager

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
}
