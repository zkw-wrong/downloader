package com.apkpure.demo.download

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.apkpure.components.downloader.db.bean.MissionDbBean
import com.apkpure.components.downloader.utils.FsUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val apkUrl1 = "https://cdn.llscdn.com/yy/files/tkzpx40x-lls-LLS-5.7-785-20171108-111118.apk"

    private lateinit var clearBt: Button
    private lateinit var apkBt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventManager.register(this)
        clearBt = findViewById(R.id.clear_bt)
        apkBt = findViewById(R.id.apk_bt)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        EventManager.unregister(this)
    }

    private fun clearDownloadFolder() {
        FsUtils.deleteFileOrDir(AppFolder.apkFolder)
    }

    private fun clickDownload() {
        AppFolder.apkFolder?.canonicalPath?.let {

        }
    }
}
