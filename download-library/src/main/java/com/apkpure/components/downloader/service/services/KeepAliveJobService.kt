package com.apkpure.components.downloader.service.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.apkpure.components.downloader.utils.AppLogger

/**
 * 此服务保护Android-LOLLIPOP以上版本的后台常驻服务不被杀死
 * @author xiongke
 * @date 2019/1/22
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class KeepAliveJobService : JobService() {
    private val logTag: String by lazy { javaClass.simpleName }
    private val mContext by lazy { this }

    companion object {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun startJob(context: Context) {
            val jobScheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val builder = JobInfo.Builder(
                    10,
                    ComponentName(context.packageName, KeepAliveJobService::class.java.name)
                )
                .setPersisted(true)
            //小于7.0
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                // 每隔3s 执行一次 job
                builder.setPeriodic(3000)
            } else {
                //延迟执行任务
                builder.setMinimumLatency(3000)
            }
            jobScheduler.schedule(builder.build())
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        //如果7.0以上 轮训
        AppLogger.d(logTag, "onStopJob")
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        //如果7.0以上 轮训
        AppLogger.d(logTag, "onStartJob")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startJob(this)
        }
        return false
    }
}
