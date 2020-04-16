package com.apkpure.demo.download

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.apkpure.components.downloader.DownloadManager
import com.apkpure.components.downloader.db.DownloadTask
import com.apkpure.components.downloader.db.Extras
import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import com.apkpure.components.downloader.misc.DownloadTaskChangeLister
import com.apkpure.components.downloader.misc.DownloadTaskFileChangeLister
import com.apkpure.components.downloader.utils.CommonUtils
import com.apkpure.components.downloader.utils.FsUtils

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val LOG_TAG = "MainActivity"
    private val apkUrl1 = "https://cdn.llscdn.com/yy/files/tkzpx40x-lls-LLS-5.7-785-20171108-111118.apk"
    private val apkUrl2 = "https://fd59c3b8ffa5957ceaf5787ea5b08f3d.dlied1.cdntips.com/godlied4.myapp.com/myapp/1104466820/cos.release-40109/10040714_com.tencent.tmgp.sgame_a713640_1.53.1.6_q6wxs5.apk?mkey=5e942cefddddbc96&f=578b&cip=221.221.154.99&proto=https\n" +
            "2020-04-13"
    private lateinit var clearBt: Button
    private lateinit var apkBt: Button
    private lateinit var deleteTaskBt: Button
    private lateinit var renameTaskBt: Button
    private lateinit var infoBt: Button
    private lateinit var pauseBt: Button
    private val downloadTaskChangeReceiver by lazy { getDownloadTaskChangeReceiver2() }
    private val getDeleteTaskDeleteReceiver by lazy { getDeleteTaskDeleteReceiver2() }
    private var downloadTask: DownloadTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clearBt = findViewById(R.id.clear_bt)
        apkBt = findViewById(R.id.apk_bt)
        deleteTaskBt = findViewById(R.id.delete_task_bt)
        renameTaskBt = findViewById(R.id.rename_task_bt)
        infoBt = findViewById(R.id.info_bt)
        pauseBt = findViewById(R.id.pause_bt)
        infoBt.setOnClickListener(this)
        clearBt.setOnClickListener(this)
        apkBt.setOnClickListener(this)
        deleteTaskBt.setOnClickListener(this)
        renameTaskBt.setOnClickListener(this)
        pauseBt.setOnClickListener(this)
        downloadTaskChangeReceiver.register()
        getDeleteTaskDeleteReceiver.register()

        //开机 恢复下载
        /* Handler().postDelayed(Runnable {
             DownloadManager.getDownloadTasks().forEach {
                 Logger.d("DownloadService","${it.absolutePath} ${it.downloadTaskStatus.name}")
                 if (it.downloadTaskStatus == DownloadTaskStatus.Waiting ||
                         it.downloadTaskStatus == DownloadTaskStatus.Downloading||
                         it.downloadTaskStatus==DownloadTaskStatus.Preparing||
                         it.downloadTaskStatus==DownloadTaskStatus.Stop) {
                     DownloadManager.resumeTask(this, it.id)
                 }
             }
         }, 3000)*/
    }

    override fun onClick(v: View?) {
        when (v) {
            clearBt -> {
                clearDownloadFolder()
            }
            apkBt -> {
                clickDownload()
            }
            deleteTaskBt -> {
                downloadTask?.let {
                    DownloadManager.deleteTask(this, arrayListOf(it.id), true)
                }
            }
            renameTaskBt -> {
                downloadTask?.let {
                    DownloadManager.renameTaskFile(this, it.id, "new_file.apk")
                }
            }
            pauseBt -> {
                downloadTask?.let {
                    DownloadManager.stopTask(this, it.id)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadTaskChangeReceiver.unregister()
        getDeleteTaskDeleteReceiver.unregister()
    }

    private fun getDownloadTaskChangeReceiver2() = DownloadTaskChangeLister.Receiver(this,
            object : DownloadTaskChangeLister.Listener {
                override fun onChange(task: DownloadTask) {
                    downloadTask = task
                    val info = when (task.downloadTaskStatus) {
                        DownloadTaskStatus.Waiting -> {
                            "等待中..."
                        }
                        DownloadTaskStatus.Preparing -> {
                            "等待中..."
                        }
                        DownloadTaskStatus.Downloading -> {
                            val progressInfo = CommonUtils.formatPercentInfo(
                                    task.currentOffset,
                                    task.totalLength
                            )
                            "下载中($progressInfo)..."
                        }
                        DownloadTaskStatus.Stop -> {
                            "暂停"
                        }
                        DownloadTaskStatus.Success -> {
                            "下载成功"
                        }
                        DownloadTaskStatus.Delete -> {
                            "已删除"
                        }
                        DownloadTaskStatus.Failed -> {
                            "下载失败"
                        }
                        DownloadTaskStatus.Retry -> {
                            "重试中"
                        }
                    }
                    infoBt.text = info
                }
            })

    //这个删除监听指的从应用里面删除才能收到消息
    private fun getDeleteTaskDeleteReceiver2() = DownloadTaskFileChangeLister.Receiver(this, object : DownloadTaskFileChangeLister.Listener {
        override fun delete(isSuccess: Boolean, downloadTaskBeanList: ArrayList<DownloadTask>?) {
            Log.d(LOG_TAG, "delete isSuccess $isSuccess size ${downloadTaskBeanList?.size}")
        }

        override fun rename(isSuccess: Boolean, downloadTask: DownloadTask?) {
            Log.d(LOG_TAG, "rename isSuccess $isSuccess")
        }
    })

    private fun clearDownloadFolder() {
        FsUtils.deleteFileOrDir(FsUtils.getDefaultDownloadDir())
    }

    private fun clickDownload() {
        DownloadManager.startNewTask(this, DownloadTask
                .Builder()
                .setUrl(apkUrl2)
                .setExtras(Extras(mutableMapOf(Pair("qwe", "123"))))
                .setFileName("王者荣耀.apk")
                //.setOverrideTaskFile(false)
                .setHeaders(Extras(mutableMapOf()))
                .setNotificationIntent(Intent(Intent.ACTION_VIEW, Uri.EMPTY, this, MainActivity::class.java))
                .setNotificationTitle("王者荣耀"))

    }
}
