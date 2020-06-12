package com.apkmatrix.components.downloader.utils

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author xiongke
 * @date 2020/6/11
 */
object ThreadManager {
    private var mThreadPollProxy: ThreadPollProxy? = null

    val threadPollProxy: ThreadPollProxy
        get() {
            if (mThreadPollProxy == null) {
                synchronized(ThreadPollProxy::class.java) {
                    if (mThreadPollProxy == null) {
                        mThreadPollProxy = ThreadPollProxy(3, 6, 1000)
                    }
                }
            }
            return mThreadPollProxy!!
        }

    class ThreadPollProxy(private val corePoolSize: Int, private val maximumPoolSize: Int, private val keepAliveTime: Long) {
        private var poolExecutor: ThreadPoolExecutor? = null

        fun execute(runnable: Runnable) {
            if (poolExecutor == null || poolExecutor!!.isShutdown) {
                poolExecutor = ThreadPoolExecutor(
                        corePoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        TimeUnit.MILLISECONDS,
                        LinkedBlockingQueue(),
                        Executors.defaultThreadFactory())
            }
            poolExecutor!!.execute(runnable)
        }
    }
}