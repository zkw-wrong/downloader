package com.apkmatrix.components.downloader.misc

import com.apkmatrix.components.downloader.db.DownloadTask

/**
 * @author xiongke
 * @date 2020/10/9
 */
internal class DownloadDataManager {
    companion object {
        private var runningTaskManager: DownloadDataManager? = null
        private val downloadTaskList = arrayListOf<DownloadTask>()

        val instance: DownloadDataManager
            get() {
                if (runningTaskManager == null) {
                    synchronized(DownloadDataManager::class.java) {
                        if (runningTaskManager == null) {
                            runningTaskManager = DownloadDataManager()
                        }
                    }
                }
                return runningTaskManager!!
            }

    }

    fun addAll(taskList: List<DownloadTask>) {
        downloadTaskList.addAll(taskList)
    }

    fun add(downloadTask: DownloadTask) {
        downloadTaskList.add(downloadTask)
    }

    fun add(index: Int, downloadTask: DownloadTask) {
        downloadTaskList.add(index, downloadTask)
    }

    fun remove(taskId: String) {
        findDownloadTaskPosition(taskId).let {
            if (it >= 0 && it < downloadTaskList.size) {
                downloadTaskList.removeAt(it)
            }
        }
    }

    fun remove(downloadTask: DownloadTask) {
        findDownloadTaskPosition(downloadTask.id).let {
            if (it >= 0 && it < downloadTaskList.size) {
                downloadTaskList.removeAt(it)
            }
        }
    }

    fun size() = downloadTaskList.size

    fun clear() {
        downloadTaskList.clear()
    }

    fun findDownloadTaskPosition(id: String): Int {
        downloadTaskList.forEachIndexed { index, downloadTask ->
            if (downloadTask.id == id) {
                return index
            }
        }
        return -1
    }

    fun findDownloadTask(id: String): DownloadTask? {
        downloadTaskList.forEach {
            if (it.id == id) {
                return it
            }
        }
        return null
    }

    fun getAll() = downloadTaskList
}