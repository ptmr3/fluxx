package com.ptmr3.fluxx

import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import javax.xml.transform.OutputKeys.METHOD

abstract class FluxxStore {

    init {
        registerActionSubscriber()
    }

    private fun registerActionSubscriber() {
        Fluxx.sInstance!!.registerActionSubscriber(this)
    }

    protected fun publishReaction(reactionId: String, vararg data: Any) {
        if (reactionId.isEmpty()) {
            throw IllegalArgumentException("Type must not be empty")
        }
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val reactionBuilder = FluxxReaction.type(reactionId)
        var i = 0
        while (i < data.size) {
            val key = data[i++] as String
            val value = data[i++]
            reactionBuilder.bundle(key, value)
        }
        val fluxReaction = reactionBuilder.build()
        Fluxx.sInstance!!.getReactionSubscriberMethods(fluxReaction)
                .subscribeOn(Schedulers.newThread())
                .subscribe { hashMap ->
                    val method = hashMap[METHOD] as Method
                    val reaction = hashMap[Fluxx.REACTION] as FluxxReaction
                    method.isAccessible = true
                    try {
                        if (method.genericParameterTypes.isEmpty()) {
                            method.invoke(hashMap[Fluxx.CLASS])
                        } else {
                            method.invoke(hashMap[Fluxx.CLASS], reaction)
                        }
                    } catch (e: Exception) {
                    }
                }
    }
}