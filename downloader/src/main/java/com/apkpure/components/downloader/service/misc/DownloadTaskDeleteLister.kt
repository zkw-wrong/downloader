package com.apkpure.components.downloader.service.misc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apkpure.components.downloader.utils.CommonUtils.register
import com.apkpure.components.downloader.utils.CommonUtils.unregister
import com.apkpure.components.downloader.utils.TaskDeleteStatusEvent

/**
 * @author xiongke
 * @date 2018/11/19
 */
class DownloadTaskDeleteLister {
    companion object {
        private val Action_Delete = DownloadTaskDeleteLister::class.java.name + ".delete"
        private val Action_All_Delete = DownloadTaskDeleteLister::class.java.name + ".All.Delete"
        private const val Params_Data = "params_data"

        fun sendDeleteBroadcast(mContext: Context, taskDeleteStatusEvent: TaskDeleteStatusEvent) {
            val intent = Intent(Action_Delete)
            intent.putExtra(Params_Data, taskDeleteStatusEvent)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }

        fun sendAllDeleteBroadcast(mContext: Context) {
            val intent = Intent(Action_All_Delete)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }
    }

    interface Listener {
        fun onDelete(taskDeleteStatusEvent: TaskDeleteStatusEvent?)
    }

    class Receiver(private val mContext: Context, private val listener: Listener) : BroadcastReceiver() {
        override fun onReceive(mContext: Context, intent: Intent) {
            try {
                when (intent.action) {
                    Action_Delete -> {
                        intent.getParcelableExtra<TaskDeleteStatusEvent>(Params_Data)?.let {
                            listener.onDelete(it)
                        }
                    }
                    Action_All_Delete -> {
                        listener.onDelete(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun register() {
            register(mContext, this, Action_All_Delete, Action_Delete)
        }

        fun unregister() {
            unregister(mContext, this)
        }
    }
}