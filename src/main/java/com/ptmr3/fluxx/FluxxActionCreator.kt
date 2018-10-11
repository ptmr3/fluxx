package com.ptmr3.fluxx

import com.ptmr3.fluxx.Fluxx.Companion.ACTION
import com.ptmr3.fluxx.Fluxx.Companion.CLASS
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.concurrent.Executors
import javax.xml.transform.OutputKeys.METHOD

abstract class FluxxActionCreator {
    private val mFluxxLog = FluxxLog.instance
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
        val dataHashMap = HashMap<String, Any>()
        var i = 0
        while (i < data.size) { dataHashMap[data[i++] as String] = data[i++] }
        val currentThread = Schedulers.from(Executors.newSingleThreadExecutor())
        Fluxx.instance.getActionSubscriberMethods(FluxxAction(actionId, dataHashMap))
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
}