package com.ptmr3.fluxx

import com.ptmr3.fluxx.Fluxx.Companion.ACTION
import com.ptmr3.fluxx.Fluxx.Companion.CLASS
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.concurrent.Executors
import javax.xml.transform.OutputKeys.METHOD

abstract class FluxxActionCreator {
    /**
     * This is the preferred method for publishing actions
     * @param actionId
     * @param data
     */
    protected fun publishAction(actionId: String, vararg data: Any) {
        if (actionId.isEmpty()) {
            throw IllegalArgumentException("Type must not be empty")
        }
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val actionBuilder = FluxxAction.type(actionId)
        var i = 0
        while (i < data.size) {
            val key = data[i++] as String
            val value = data[i++]
            actionBuilder.bundle(key, value)
        }
        val currentThread = Schedulers.from(Executors.newSingleThreadExecutor())
        Fluxx.sInstance!!.getActionSubscriberMethods(actionBuilder.build())
                .subscribeOn(Schedulers.newThread()).observeOn(currentThread)
                .blockingSubscribe { hashMap ->
                    val method = hashMap[METHOD] as Method
                    method.isAccessible = true
                    try {
                        method.invoke(hashMap[CLASS], hashMap[ACTION])
                    } catch (e: Exception) { }
                }
    }
}