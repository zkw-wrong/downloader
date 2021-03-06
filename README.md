# Apkpure Downloader

[Github OkDownload](https://github.com/lingochamp/okdownload)

[最新版引用地址](http://maven.302e.com:3080/#browse/browse:maven-releases:com/apkmatrix/components/downloader)

- 添加引用
```
//无需引用GitHub的下载库
maven { url 'http://maven.302e.com:3080/repository/maven-public/' }

implementation 'com.apkmatrix.components:downloader:{版本号}'
```

-  应用初始化初始化
```
DownloadManager.initial(this, OkHttpClient.Builder()
                               .connectTimeout(10, TimeUnit.SECONDS)
                               .readTimeout(10, TimeUnit.SECONDS)
                               .writeTimeout(10, TimeUnit.SECONDS)
                               .retryOnConnectionFailure(true))
//DownloadManager.setDebug(true)
//DownloadManager.setNotificationLargeIcon(it)
```
- 注册监听
```
//下载回调监听
DownloadTaskChangeReceiver.Receiver(this,
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
DownloadTaskFileChangeReceiver.Receiver(this, object : DownloadTaskFileChangeLister.Listener {
        override fun delete(isSuccess: Boolean, downloadTask: ArrayList<DownloadTaskBean>?) {
            Log.d(LOG_TAG, "delete isSuccess $isSuccess size ${downloadTask?.size}")
        }

        override fun rename(isSuccess: Boolean, downloadTaskBean: DownloadTaskBean?) {
            Log.d(LOG_TAG, "rename isSuccess $isSuccess")
        }
    })
```
- 调用 
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
[DownloadManager](https://apk.302e.com:3443/mobile/components/downloader/-/blob/master/downloader/src/main/java/com/apkmatrix/components/downloader/DownloadManager.kt)

- 小技巧
```
1.通知点击进来如果数据没有更新完成,判断downloadManager是否更新完成，更新完成有回调，或者主动调用更新数据方法具体看DownloadManager
2.尽量不要初始化很多次
3.很多相关数据存放在Extra中
4.最大下载个数设置调用方法之前，立即修改不一定生效 具体看GitHub OkDownload下载库
5.里面加了流量和权限提示弹窗，如果适配多语言替换String资源
6.删除回调注意别写错了 删除多个任务的时候downloadTaskList为空 单个时候任务时候里面只有一个具体自己打log
7.默认是不覆盖文件 新名字和绝对路径 优先级 绝对路径>默认路径/新名字>默认路径/hashCode
8.里面写了2个Service适配Android 8.0+系统
9.log日志默认是关闭的 打replace包请关闭
10.通知的intent中不可携带参数
11.targetSdkVersion<=29 不支持Android 11
```
- 权限
```kotlin
调用会请求存储权限
下载权限需要实现BaseAppInterface接口 或者直接引用BaseAppActivityUi的包
具体包再服务器上这里不作解释
```
- 混淆
```kotlin
# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }

# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-dontwarn okio.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# okdownload
-keep class com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite {
    public com.liulishuo.okdownload.core.breakpoint.DownloadStore createRemitSelf();
    public com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite(android.content.Context);
}
```
 