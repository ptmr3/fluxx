package com.ptmr3.fluxx

import java.lang.reflect.Method

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import javax.xml.transform.OutputKeys.METHOD

abstract class FluxStore(private val msFluxInstance: Flux) {

    protected fun emitReaction(reactionId: String, vararg data: Any) {
        if (reactionId.isEmpty()) {
            throw IllegalArgumentException("Type must not be empty")
        }
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val reactionBuilder = FluxReaction.type(reactionId)
        var i = 0
        while (i < data.size) {
            val key = data[i++] as String
            val value = data[i++]
            reactionBuilder.bundle(key, value)
        }
        val fluxReaction = reactionBuilder.build()
        msFluxInstance.emitReaction(fluxReaction).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe{ hashMap ->
                    val method = hashMap[METHOD] as Method
                    val reaction = hashMap[Flux.REACTION] as FluxReaction
                    method.isAccessible = true
                    try {
                        method.invoke(hashMap[Flux.CLASS], reaction)
                    } catch (e: Exception) {
                    }
                }
    }
}
