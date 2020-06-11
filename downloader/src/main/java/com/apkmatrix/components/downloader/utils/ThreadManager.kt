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
                poolExecutor = ThreadPoolExecutor( //核心线程数量
                        corePoolSize,  //最大线程数量
                        maximumPoolSize,  //当线程空闲时，保持活跃的时间
                        keepAliveTime,  //时间单元 ，毫秒级
                        TimeUnit.MILLISECONDS,  //线程任务队列
                        LinkedBlockingQueue(),  //创建线程的工厂
                        Executors.defaultThreadFactory())
            }
            poolExecutor!!.execute(runnable)
        }
    }
}