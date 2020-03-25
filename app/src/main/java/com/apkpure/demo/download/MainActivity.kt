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
import com.apkpure.components.downloader.DownloadLaunchUtils
import com.apkpure.components.downloader.db.bean.MissionDbBean
import com.apkpure.components.downloader.db.enums.MissionStatusType
import com.apkpure.components.downloader.utils.FormatUtils
import com.apkpure.components.downloader.utils.FsUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val apkUrl1 =
        "https://cdn.llscdn.com/yy/files/tkzpx40x-lls-LLS-5.7-785-20171108-111118.apk"
    private val call_write_storage = 1
    private lateinit var clearBt: Button
    private lateinit var apkBt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventManager.register(this)
        clearBt = findViewById(R.id.clear_bt)
        apkBt = findViewById(R.id.apk_bt)
        clearBt.setOnClickListener(this)
        apkBt.setOnClickListener(this)
        checkPermissions()
    }

    override fun onClick(v: View?) {
        if (v == clearBt) {
            clearDownloadFolder()
        } else if (v == apkBt) {
            clickDownload()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changeDownload(missionDbBean: MissionDbBean) {
        apkBt.isEnabled = false
        val info = when (missionDbBean.missionStatusType) {
            MissionStatusType.Waiting -> {
                "等待中..."
            }
            MissionStatusType.Preparing -> {
                "等待中..."
            }
            MissionStatusType.Downloading -> {
                val progressInfo = FormatUtils.formatPercentInfo(
                    missionDbBean.currentOffset,
                    missionDbBean.totalLength
                )
                "下载中($progressInfo)..."
            }
            MissionStatusType.Stop -> {
                "暂停"
            }
            MissionStatusType.Success -> {
                "下载成功"
            }
            MissionStatusType.Delete -> {
                "已删除"
            }
            MissionStatusType.Failed -> {
                "下载失败"
            }
            MissionStatusType.Retry -> {
                "重试中"
            }
        }
        apkBt.text = info
    }

    override fun onDestroy() {
        super.onDestroy()
        EventManager.unregister(this)
    }

    private fun clearDownloadFolder() {
        apkBt.isEnabled = true
        apkBt.text = "重新下载"
        FsUtils.deleteFileOrDir(AppFolder.apkFolder)
    }

    private fun clickDownload() {
        AppFolder.apkFolder?.absolutePath?.let {
            DownloadLaunchUtils.startClickTask(this, MissionDbBean().apply {
                val fileName = "test.apk"
                this.url = apkUrl1
                this.absolutePath = "$it${File.separator}$fileName"
                this.showNotification = true
                this.flag = 1//file type
                this.shortName = "test.apk"
            })
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
