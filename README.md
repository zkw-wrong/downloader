# Apkpuew Downloader

[最新版引用地址](http://maven.302e.com:3080/#browse/browse:maven-releases:com%2Fapkpure%2Fcomponents%2Fdownloader)

1. 添加引用
```
maven { url 'http://maven.302e.com:3080/repository/maven-public/' }

implementation 'com.apkpure.components:downloader:{版本号}'
```

2. 应用初始化初始化
```
DownloadManager.initial(this, OkHttpClient.Builder()
                               .connectTimeout(10, TimeUnit.SECONDS)
                               .readTimeout(10, TimeUnit.SECONDS)
                               .writeTimeout(10, TimeUnit.SECONDS)
                               .retryOnConnectionFailure(true))
//DownloadManager.setDebug(true)
//DownloadManager.setNotificationLargeIcon(it)
```
3.注册监听
```
//下载回调监听
DownloadTaskChangeLister.Receiver(this,
            object : DownloadTaskChangeLister.Listener {
                override fun onChange(downloadTaskBean1: DownloadTaskBean) {
                    val info = when (downloadTaskBean1.downloadTaskStatusType) {
                        DownloadTaskStatusType.Waiting -> {
                            "等待中..."
                        }
                        DownloadTaskStatusType.Preparing -> {
                            "等待中..."
                        }
                        DownloadTaskStatusType.Downloading -> {
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
                }
            })

//下载任务单个删除，全部删除，重命名，下载文件回调
DownloadTaskFileChangeLister.Receiver(this, object : DownloadTaskFileChangeLister.Listener {
        override fun delete(isSuccess: Boolean, downloadTaskBeanList: ArrayList<DownloadTaskBean>?) {
            Log.d(LOG_TAG, "delete isSuccess $isSuccess size ${downloadTaskBeanList?.size}")
        }

        override fun rename(isSuccess: Boolean, downloadTaskBean: DownloadTaskBean?) {
            Log.d(LOG_TAG, "rename isSuccess $isSuccess")
        }
    })
```
4.调用 
```
//下载
DownloadManager.startNewTask(this, DownloadTask
                .Builder()
                .setUrl(apkUrl1)
                .setExtras(Extras(mutableMapOf(Pair("qwe", "123"))))
                .setFileName("abc.apk")
                .setOverrideTaskFile(true)
                .setHeaders(Extras(mutableMapOf()))
                .setNotificationIntent(Intent(Intent.ACTION_VIEW, Uri.EMPTY, this, MainActivity::class.java))
                .setNotificationTitle("Title Abc 123"))
//文件重命名
DownloadManager.renameTaskFile(this, it.id, "new_file.apk")
//删除任务

//暂停
//删除全部任务
具体查看 DownloadManager 类的接口
```
[DownloadManager](https://apk.302e.com:3443/mobile/components/downloader/-/blob/master/downloader/src/main/java/com/apkpure/components/downloader/service/DownloadManager.kt)

5.其他
```

```
 
