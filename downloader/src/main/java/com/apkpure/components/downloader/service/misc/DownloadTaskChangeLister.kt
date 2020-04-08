package com.apkpure.components.downloader.service.misc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apkpure.components.downloader.db.DownloadTask
import com.apkpure.components.downloader.utils.CommonUtils.register
import com.apkpure.components.downloader.utils.CommonUtils.unregister

/**
 * @author xiongke
 * @date 2018/11/19
 */
class DownloadTaskChangeLister {
    companion object {
        private val Action_Change = DownloadTaskChangeLister::class.java.name + ".change"
        private const val PARAM_DATA = "PARAM_DATA"
        fun sendChangeBroadcast(mContext: Context, downloadTask: DownloadTask) {
            val intent = Intent(Action_Change)
            intent.putExtra(PARAM_DATA, downloadTask)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }
    }

    interface Listener {
        fun onChange(downloadTask: DownloadTask)
    }

    open class Receiver(private val mContext: Context, private val listener: Listener) :
            BroadcastReceiver() {
        override fun onReceive(mContext: Context, intent: Intent) {
            try {
                when (intent.action) {
                    Action_Change -> {
                        intent.getParcelableExtra<DownloadTask>(PARAM_DATA)?.let {
                            listener.onChange(it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun register() {
            register(mContext, this, Action_Change)
        }

        fun unregister() {
            unregister(mContext, this)
        }
    }
}