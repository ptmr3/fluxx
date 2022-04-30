package com.ptmr3.fluxx

import com.ptmr3.fluxx.Fluxx.Companion.ACTION
import com.ptmr3.fluxx.Fluxx.Companion.CLASS
import com.ptmr3.fluxx.Fluxx.Companion.METHOD
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.concurrent.Executors

abstract class ActionsCreator {
    private val mFluxxLog = FluxxLog.instance
    /**
     * This is the preferred method for publishing actions
     * @param actionId
     * @param data
     */
    protected fun publishAction(actionId: String, vararg data: Any) {
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val dataHashMap = HashMap<String, Any>()
        var i = 0
        while (i < data.size) { dataHashMap[data[i++] as String] = data[i++] }
        val currentThread = Schedulers.from(Executors.newSingleThreadExecutor())
        Fluxx.instance.getActionSubscriberMethods(Action(actionId, dataHashMap))
            .subscribeOn(Schedulers.io()).observeOn(currentThread)
            .blockingSubscribe { hashMap ->
                val method = hashMap[METHOD] as Method
                method.isAccessible = true
                try {
                    method.invoke(hashMap[CLASS], hashMap[ACTION])
                    mFluxxLog.print("ACTION: $actionId, ${data.toList()} -> ${hashMap[CLASS]?.javaClass?.simpleName}, ${hashMap[ACTION]}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    protected fun publishParallelAction(actionId: String, vararg data: Any) {
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val dataHashMap = HashMap<String, Any>()
        var i = 0
        while (i < data.size) { dataHashMap[data[i++] as String] = data[i++] }
        Fluxx.instance.getActionSubscriberMethods(Action(actionId, dataHashMap))
            .flatMap {
                Observable.just(it)
                    .subscribeOn(Schedulers.io())
                    .map { hashMap ->
                        val method = hashMap[METHOD] as Method
                        method.isAccessible = true
                        try {
                            method.invoke(hashMap[CLASS], hashMap[ACTION])
                            mFluxxLog.print("ACTION: $actionId, ${data.toList()} -> ${hashMap[CLASS]?.javaClass?.simpleName}, ${hashMap[ACTION]}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }.subscribe()
    }
}