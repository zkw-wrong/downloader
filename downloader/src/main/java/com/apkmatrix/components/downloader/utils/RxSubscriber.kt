package com.apkmatrix.components.downloader.utils

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by Xiong Ke on 2017/7/27.
 */

abstract class RxSubscriber<T> : Observer<T> {
    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(t: T) {
        rxOnNext(t)
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        rxOnError(Exception(e))
    }

    override fun onComplete() {

    }

    abstract fun rxOnNext(t: T)

    abstract fun rxOnError(e: Exception)
}
