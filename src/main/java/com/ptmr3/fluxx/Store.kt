package com.ptmr3.fluxx

import com.ptmr3.fluxx.Fluxx.Companion.CLASS
import com.ptmr3.fluxx.Fluxx.Companion.REACTION
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.reflect.Method
import java.util.concurrent.Executors
import javax.xml.transform.OutputKeys.METHOD

abstract class Store {
    private val mFluxxLog = FluxxLog.instance

    init {
        registerActionSubscriber()
    }

    private fun registerActionSubscriber() {
        Fluxx.instance.registerActionSubscriber(this)
    }

    protected fun publishReaction(reactionId: String, vararg data: Any) {
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val dataHashMap = HashMap<String, Any>()
        var i = 0
        while (i < data.size) {
            dataHashMap[data[i++] as String] = data[i++]
        }
        val currentThread = Schedulers.from(Executors.newSingleThreadExecutor())
        Fluxx.instance.getReactionSubscriberMethods(Reaction(reactionId, dataHashMap))
            .subscribeOn(Schedulers.io()).observeOn(currentThread)
            .blockingSubscribe { hashMap ->
                val method = hashMap[METHOD] as Method
                method.isAccessible = true
                try {
                    if (method.genericParameterTypes.isEmpty()) {
                        method.invoke(hashMap[CLASS])
                        mFluxxLog.print("publishReaction: $reactionId, ${data.asList()} -> ${hashMap[CLASS]?.javaClass?.simpleName}")
                    } else {
                        method.invoke(hashMap[CLASS], hashMap[REACTION])
                        mFluxxLog.print("REACTION: $reactionId, ${data.asList()} -> ${hashMap[CLASS]?.javaClass?.simpleName}, ${hashMap[REACTION]}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    protected fun publishFailureReaction(reactionId: String, vararg data: Any) {
        if (data.size % 2 != 0) {
            throw IllegalArgumentException("Data must be a valid list of key,value pairs")
        }
        val dataHashMap = HashMap<String, Any>()
        var i = 0
        while (i < data.size) {
            dataHashMap[data[i++] as String] = data[i++]
        }
        val currentThread = Schedulers.from(Executors.newSingleThreadExecutor())
        Fluxx.instance.getReactionSubscriberMethods(Reaction(reactionId, dataHashMap))
            .subscribeOn(Schedulers.io()).observeOn(currentThread)
            .blockingSubscribe { hashMap ->
                val method = hashMap[METHOD] as Method
                method.isAccessible = true
                try {
                    if (method.genericParameterTypes.isEmpty()) {
                        method.invoke(hashMap[CLASS])
                        mFluxxLog.print("publishReaction: $reactionId, ${data.asList()} -> ${hashMap[CLASS]?.javaClass?.simpleName}")
                    } else {
                        method.invoke(hashMap[CLASS], hashMap[REACTION])
                        mFluxxLog.print("REACTION: $reactionId, ${data.asList()} -> ${hashMap[CLASS]?.javaClass?.simpleName}, ${hashMap[REACTION]}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }
}