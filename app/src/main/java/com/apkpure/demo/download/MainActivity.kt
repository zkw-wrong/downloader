package com.apkpure.demo.download

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apkpure.components.downloader.db.DownloadTask
import com.apkpure.components.downloader.db.Extras
import com.apkpure.components.downloader.db.enums.DownloadTaskStatus
import com.apkpure.components.downloader.service.DownloadManager
import com.apkpure.components.downloader.service.misc.DownloadTaskChangeLister
import com.apkpure.components.downloader.service.misc.DownloadTaskFileChangeLister
import com.apkpure.components.downloader.utils.CommonUtils

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val LOG_TAG = "MainActivity"
    private val apkUrl1 = "https://cdn.llscdn.com/yy/files/tkzpx40x-lls-LLS-5.7-785-20171108-111118.apk"
    private val call_write_storage = 1
    private lateinit var clearBt: Button
    private lateinit var apkBt: Button
    private lateinit var deleteTaskBt: Button
    private lateinit var renameTaskBt: Button
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
        clearBt.setOnClickListener(this)
        apkBt.setOnClickListener(this)
        deleteTaskBt.setOnClickListener(this)
        renameTaskBt.setOnClickListener(this)
        downloadTaskChangeReceiver.register()
        getDeleteTaskDeleteReceiver.register()
        renameTaskBt.isEnabled = false
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
                DownloadManager.instance.deleteTask(this, arrayListOf(apkUrl1), true)
            }
            renameTaskBt -> {
                downloadTask?.let {
                    DownloadManager.instance.renameTaskFile(this, it.url, "new_file.apk")
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
                override fun onChange(downloadTask1: DownloadTask) {
                    downloadTask = downloadTask1
                    apkBt.isEnabled = false
                    renameTaskBt.isEnabled = downloadTask1.downloadTaskStatus == DownloadTaskStatus.Success
                    val info = when (downloadTask1.downloadTaskStatus) {
                        DownloadTaskStatus.Waiting -> {
                            "等待中..."
                        }
                        DownloadTaskStatus.Preparing -> {
                            "等待中..."
                        }
                        DownloadTaskStatus.Downloading -> {
                            val progressInfo = CommonUtils.formatPercentInfo(
                                    downloadTask1.currentOffset,
                                    downloadTask1.totalLength
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
                    apkBt.text = info
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
        apkBt.isEnabled = true
        apkBt.text = "重新下载"
        DownloadManager.instance.deleteAllTask(this)
    }

    private fun clickDownload() {
        DownloadManager.instance.startTask(this, apkUrl1, "abc.apk"
                , "Title ABC", Extras(mutableMapOf(Pair("qwe", "123"))))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == call_write_storage) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                apkBt.isEnabled = true
                clearBt.isEnabled = true
            } else {
                Toast.makeText(this, "读写权限拒绝,无法测试!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
