package com.ptmr3.fluxx

import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
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
        val fluxAction = actionBuilder.build()
        //TODO null check on calls to instance to return error message
        Fluxx.sInstance!!.getActionSubscriberMethods(fluxAction)
                .subscribeOn(Schedulers.newThread())
                .subscribe { hashMap ->
                    val method = hashMap[METHOD] as Method
                    val action = hashMap[Fluxx.ACTION] as FluxxAction
                    method.isAccessible = true
                    try {
                        method.invoke(hashMap[Fluxx.CLASS], action)
                    } catch (e: Exception) {

                    }
                }
    }
}