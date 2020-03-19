package com.apkpure.components.downloader.db.other

/**
 * @author xiongke
 * @date 2018/11/6
 */
enum class TaskStatus(val typeId: Int) {
    Waiting(0),
    Preparing(1),
    Stop(2),
    Downloading(3),
    Success(4),
    Failed(5),
    Delete(6),
    Retry(7)
}