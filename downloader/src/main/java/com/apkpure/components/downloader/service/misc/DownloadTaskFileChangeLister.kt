package com.apkpure.components.downloader.service.misc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apkpure.components.downloader.db.DownloadTaskBean
import com.apkpure.components.downloader.utils.CommonUtils.register
import com.apkpure.components.downloader.utils.CommonUtils.unregister

/**
 * @author xiongke
 * @date 2018/11/19
 */
class DownloadTaskFileChangeLister {
    companion object {
        private val ActionDelete = DownloadTaskFileChangeLister::class.java.name + ".delete"
        private val ActionRename = DownloadTaskFileChangeLister::class.java.name + ".file_rename"
        private const val paramsData = "params_data"
        private const val paramsIsSuccess = "is_success"

        fun sendDeleteBroadcast(mContext: Context, downloadTaskBeanList: ArrayList<DownloadTaskBean>?, isSuccess: Boolean) {
            val intent = Intent(ActionDelete)
            intent.putParcelableArrayListExtra(paramsData, downloadTaskBeanList)
            intent.putExtra(paramsIsSuccess, isSuccess)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }

        fun sendRenameBroadcast(mContext: Context, downloadTaskBean: DownloadTaskBean?, isSuccess: Boolean) {
            val intent = Intent(ActionRename)
            intent.putExtra(paramsIsSuccess, isSuccess)
            intent.putExtra(paramsData, downloadTaskBean)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }
    }

    interface Listener {
        fun delete(isSuccess: Boolean, downloadTaskBeanList: ArrayList<DownloadTaskBean>?)

        fun rename(isSuccess: Boolean, downloadTaskBean: DownloadTaskBean?)
    }

    class Receiver(private val mContext: Context, private val listener: Listener) : BroadcastReceiver() {
        override fun onReceive(mContext: Context, intent: Intent) {
            try {
                when (intent.action) {
                    ActionDelete -> {
                        val downloadTaskBean = intent.getParcelableArrayListExtra<DownloadTaskBean>(paramsData)
                        val isSuccess = intent.getBooleanExtra(paramsIsSuccess, false)
                        listener.delete(isSuccess, downloadTaskBean)
                    }
                    ActionRename -> {
                        val downloadTaskBean = intent.getParcelableExtra<DownloadTaskBean>(paramsData)
                        val isSuccess = intent.getBooleanExtra(paramsIsSuccess, false)
                        listener.rename(isSuccess, downloadTaskBean)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun register() {
            register(mContext, this, ActionDelete, ActionRename)
        }

        fun unregister() {
            unregister(mContext, this)
        }
    }
}