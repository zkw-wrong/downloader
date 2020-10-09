package com.apkmatrix.components.downloader.misc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apkmatrix.components.downloader.db.DownloadTask
import com.apkmatrix.components.downloader.utils.CommonUtils

/**
 * @author xiongke
 * @date 2018/11/19
 */
class DownloadTaskFileReceiver {
    companion object {
        private val ActionDelete = DownloadTaskFileReceiver::class.java.name + ".delete"
        private val ActionRename = DownloadTaskFileReceiver::class.java.name + ".file_rename"
        private const val paramsData = "params_data"
        private const val paramsIsSuccess = "is_success"

        fun sendDeleteBroadcast(mContext: Context, downloadTask: DownloadTask?, isSuccess: Boolean) {
            val intent = Intent(ActionDelete)
            intent.putExtra(paramsData, downloadTask)
            intent.putExtra(paramsIsSuccess, isSuccess)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }

        fun sendRenameBroadcast(mContext: Context, downloadTask: DownloadTask?, isSuccess: Boolean) {
            val intent = Intent(ActionRename)
            intent.putExtra(paramsIsSuccess, isSuccess)
            intent.putExtra(paramsData, downloadTask)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }
    }

    interface Listener {
        fun delete(isSuccess: Boolean, downloadTask: DownloadTask?)

        fun rename(isSuccess: Boolean, downloadTask: DownloadTask?)
    }

    class Receiver(private val mContext: Context, private val listener: Listener) : BroadcastReceiver() {
        override fun onReceive(mContext: Context, intent: Intent) {
            try {
                when (intent.action) {
                    ActionDelete -> {
                        val downloadTask = intent.getParcelableExtra<DownloadTask>(paramsData)
                        val isSuccess = intent.getBooleanExtra(paramsIsSuccess, false)
                        listener.delete(isSuccess, downloadTask)
                    }
                    ActionRename -> {
                        val downloadTask = intent.getParcelableExtra<DownloadTask>(paramsData)
                        val isSuccess = intent.getBooleanExtra(paramsIsSuccess, false)
                        listener.rename(isSuccess, downloadTask)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun register() {
            CommonUtils.register(mContext, this, ActionDelete, ActionRename)
        }

        fun unregister() {
            CommonUtils.unregister(mContext, this)
        }
    }
}