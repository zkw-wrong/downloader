package com.apkpure.components.downloader

import android.content.Context
import android.content.Intent
import android.os.Build
import com.apkpure.components.downloader.db.bean.MissionDbBean
import com.apkpure.components.downloader.service.services.DownloadServiceAssistUtils
import com.apkpure.components.downloader.service.services.DownloadServiceV14
import com.apkpure.components.downloader.service.services.DownloadServiceV21

/**
 * author: mr.xiong
 * date: 2020/3/25
 */
object DownloadLaunchUtils {

    fun startClickTask(mContext: Context, missionDbBean: MissionDbBean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newStartIntent(mContext, DownloadServiceV21::class.java, missionDbBean).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newStartIntent(mContext,
                DownloadServiceV14::class.java, missionDbBean))
        }
    }

    fun stopTask(mContext: Context, missionDbBean: MissionDbBean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newStopIntent(mContext, DownloadServiceV21::class.java, missionDbBean).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newStopIntent(mContext,
                DownloadServiceV14::class.java, missionDbBean))
        }
    }

    fun deleteTask(mContext: Context, missionDbBean: MissionDbBean, isDeleteFile: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newDeleteIntent(mContext, DownloadServiceV21::class.java, missionDbBean, isDeleteFile).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newDeleteIntent(mContext,
                DownloadServiceV14::class.java, missionDbBean, isDeleteFile))
        }
    }

    fun startNotCompatTask(mContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newStartNotCompatIntent(mContext, DownloadServiceV21::class.java).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newStartNotCompatIntent(mContext,
                DownloadServiceV14::class.java))
        }
    }

    fun deleteAllTask(mContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DownloadServiceAssistUtils.newDeleteAllIntent(mContext, DownloadServiceV21::class.java).apply {
                DownloadServiceV21.enqueueWorkService(mContext, this)
            }
        } else {
            startService(mContext, DownloadServiceAssistUtils.newDeleteAllIntent(mContext,
                DownloadServiceV14::class.java))
        }
    }


   private fun startService(mContext: Context, intent: Intent) {
        mContext.startService(intent)
    }

    private fun startForegroundService(mContext: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent)
        } else {
            mContext.startService(intent)
        }
    }
}