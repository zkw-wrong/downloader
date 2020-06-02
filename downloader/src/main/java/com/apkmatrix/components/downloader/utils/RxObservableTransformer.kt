package com.apkmatrix.components.downloader.utils

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers


/**
 * 统一线程处理
 *
 * @author Xiong Ke
 * @date 2017/12/27
 */

object RxObservableTransformer {

    fun <T> io_main(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
        }
    }

    fun <T> io_io(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
        }
    }

    fun <T> main_main(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
        }
    }

    fun <T> errorResult(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.onErrorResumeNext(Function { Observable.error(Exception(it)) })
        }
    }

    fun <T> createData(t: T): Observable<T> {
        return Observable.create { observableEmitter ->
            if (!observableEmitter.isDisposed) {
                observableEmitter.onNext(t)
                observableEmitter.onComplete()
            }
        }
    }
}
