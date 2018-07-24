package com.ptmr3.fluxx

import com.ptmr3.fluxx.Fluxx.Companion.CLASS
import com.ptmr3.fluxx.Fluxx.Companion.REACTION
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.concurrent.Executors
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
        val currentThread = Schedulers.from(Executors.newSingleThreadExecutor())
        Fluxx.sInstance!!.getReactionSubscriberMethods(reactionBuilder.build())
                .subscribeOn(Schedulers.newThread()).observeOn(currentThread)
                .blockingSubscribe { hashMap ->
                    val method = hashMap[METHOD] as Method
                    method.isAccessible = true
                    try {
                        if (method.genericParameterTypes.isEmpty()) {
                            method.invoke(hashMap[CLASS])
                        } else {
                            method.invoke(hashMap[CLASS], hashMap[REACTION])
                        }
                    } catch (e: Exception) { }
                }
    }
}