package com.apkpure.demo.download

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.apkpure.components.downloader.db.bean.DownloadTaskBean
import com.apkpure.components.downloader.db.enums.DownloadTaskStatusType
import com.apkpure.components.downloader.service.DownloadManager
import com.apkpure.components.downloader.service.misc.DownloadTaskChangeLister
import com.apkpure.components.downloader.service.misc.DownloadTaskDeleteLister
import com.apkpure.components.downloader.utils.FormatUtils
import com.apkpure.components.downloader.utils.FsUtils
import com.apkpure.components.downloader.utils.TaskDeleteStatusEvent
import java.io.File

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val apkUrl1 = "https://cdn.llscdn.com/yy/files/tkzpx40x-lls-LLS-5.7-785-20171108-111118.apk"
    private val call_write_storage = 1
    private lateinit var clearBt: Button
    private lateinit var apkBt: Button
    private lateinit var deleteTaskBt: Button
    private val downloadTaskChangeReceiver by lazy { getDownloadTaskChangeReceiver2() }
    private val getDeleteTaskDeleteReceiver by lazy { getDeleteTaskDeleteReceiver2() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clearBt = findViewById(R.id.clear_bt)
        apkBt = findViewById(R.id.apk_bt)
        deleteTaskBt = findViewById(R.id.delete_task_bt)
        clearBt.setOnClickListener(this)
        apkBt.setOnClickListener(this)
        deleteTaskBt.setOnClickListener(this)
        checkPermissions()
        downloadTaskChangeReceiver.register()
        getDeleteTaskDeleteReceiver.register()
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
                DownloadManager.instance.deleteTask(this, apkUrl1, true)
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
                override fun onChange(downloadTaskBean: DownloadTaskBean) {
                    apkBt.isEnabled = false
                    val info = when (downloadTaskBean.downloadTaskStatusType) {
                        DownloadTaskStatusType.Waiting -> {
                            "等待中..."
                        }
                        DownloadTaskStatusType.Preparing -> {
                            "等待中..."
                        }
                        DownloadTaskStatusType.Downloading -> {
                            val progressInfo = FormatUtils.formatPercentInfo(
                                    downloadTaskBean.currentOffset,
                                    downloadTaskBean.totalLength
                            )
                            "下载中($progressInfo)..."
                        }
                        DownloadTaskStatusType.Stop -> {
                            "暂停"
                        }
                        DownloadTaskStatusType.Success -> {
                            "下载成功"
                        }
                        DownloadTaskStatusType.Delete -> {
                            "已删除"
                        }
                        DownloadTaskStatusType.Failed -> {
                            "下载失败"
                        }
                        DownloadTaskStatusType.Retry -> {
                            "重试中"
                        }
                    }
                    apkBt.text = info
                }
            })

    //这个删除监听指的从应用里面删除才能收到消息
    private fun getDeleteTaskDeleteReceiver2() = DownloadTaskDeleteLister.Receiver(this, object : DownloadTaskDeleteLister.Listener {
        override fun onDelete(taskDeleteStatusEvent: TaskDeleteStatusEvent?) {
            Toast.makeText(this@MainActivity, "手动删除任务成功!", Toast.LENGTH_LONG).show()
        }
    })

    fun clearDownloadFolder() {
        apkBt.isEnabled = true
        apkBt.text = "重新下载"
        FsUtils.deleteFileOrDir(AppFolder.apkFolder)
    }

    private fun clickDownload() {
        AppFolder.apkFolder?.absolutePath?.let {
            DownloadManager.instance.startClickTask(this, DownloadTaskBean().apply {
                val fileName = "test.apk"
                this.url = apkUrl1
                this.absolutePath = "$it${File.separator}$fileName"
                this.showNotification = true
                this.flag = 1//file type
                this.shortName = "test.apk"
                this.paramData = "JSON 参数信息"
            })
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            apkBt.isEnabled = false
            clearBt.isEnabled = false
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    call_write_storage
            )
        } else {
            apkBt.isEnabled = true
            clearBt.isEnabled = true
        }
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
