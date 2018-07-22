package com.ptmr3.fluxx

interface FluxxReactionSubscriber {
    fun registerReactionSubscriber(reactionSubscriberClass: Any) {
        Fluxx.sInstance!!.registerReactionSubscriber(reactionSubscriberClass)
    }

    fun unregisterReactionSubscriber(reactionSubscriberClass: Any) {
        Fluxx.sInstance!!.unregisterReactionSubscriber(reactionSubscriberClass)
    }
}