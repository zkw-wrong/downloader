package com.apkpure.components.downloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * author: mr.xiong
 * date: 2020/3/26
 */
object CommonUtils {

    fun register(mContext: Context?, receiver: BroadcastReceiver?, vararg actions: String?) {
        val filter = IntentFilter()
        for (action in actions) {
            filter.addAction(action)
        }
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(receiver!!, filter)
    }

    fun unregister(mContext: Context?, receiver: BroadcastReceiver?) {
        LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(receiver!!)
    }
}