# downloader

1. 添加引用
```
maven {
    url 'http://maven.302e.com:3080/repository/maven-public/'
    }
implementation 'com.apkpure.components:downloader:1.2'

```

2. 初始化
```
  DownloadManager.initial(this, OkHttpClient.Builder()
                                                .connectTimeout(10, TimeUnit.SECONDS)
                                                .readTimeout(10, TimeUnit.SECONDS)
                                                .writeTimeout(10, TimeUnit.SECONDS)
                                                .retryOnConnectionFailure(true))
```