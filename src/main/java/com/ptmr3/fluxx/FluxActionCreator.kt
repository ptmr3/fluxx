package com.ptmr3.fluxx

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import javax.xml.transform.OutputKeys.METHOD

abstract class FluxActionCreator(private val mFluxInstance: Flux) {
    /**
     * This is the preferred method for posting actions to the subscription manager
     * @param actionId
     * @param data
     */
    protected fun emitAction(actionId: String, vararg data: Any) {
        if (actionId.isEmpty()) {
            throw IllegalArgumentException("Type must not be empty")
        }
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val actionBuilder = FluxAction.type(actionId)
        var i = 0
        while (i < data.size) {
            val key = data[i++] as String
            val value = data[i++]
            actionBuilder.bundle(key, value)
        }
        val fluxAction = actionBuilder.build()
        mFluxInstance.emitAction(fluxAction)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { hashMap ->
                    val method = hashMap[METHOD] as Method
                    val action = hashMap[Flux.ACTION] as com.ptmr3.fluxx.FluxAction
                    method.isAccessible = true
                    try {
                        method.invoke(hashMap[Flux.CLASS], action)
                    } catch (e: Exception) {

                    }
                }
    }
}
