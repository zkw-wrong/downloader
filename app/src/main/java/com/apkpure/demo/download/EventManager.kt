package com.apkpure.demo.download

import org.greenrobot.eventbus.EventBus

/**
 * Created by Xiong Ke on 2017/8/11.
 */

object EventManager {

    fun register(subscriber: Any) {
        EventBus.getDefault().register(subscriber)
    }

    fun unregister(subscriber: Any) {
        EventBus.getDefault().unregister(subscriber)
    }

    fun post(event: Any) {
        EventBus.getDefault().post(event)
    }
}
