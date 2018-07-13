package com.ptmr3.fluxx

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Method
import javax.xml.transform.OutputKeys.METHOD

abstract class FluxxStore(private val mFluxx: Fluxx) {

    fun register() {
        mFluxx.registerActionSubscriber(this)
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
        mFluxx.getReactionSubscriberMethods(fluxReaction)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { hashMap ->
                    val method = hashMap[METHOD] as Method
                    val reaction = hashMap[Fluxx.REACTION] as FluxxReaction
                    method.isAccessible = true
                    try {
                        method.invoke(hashMap[Fluxx.CLASS], reaction)
                    } catch (e: Exception) {
                    }
                }
    }
}