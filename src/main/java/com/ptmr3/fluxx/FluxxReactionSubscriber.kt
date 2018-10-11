package com.ptmr3.fluxx

interface FluxxReactionSubscriber {
    fun registerReactionSubscriber(reactionSubscriberClass: Any) {
        Fluxx.instance.registerReactionSubscriber(reactionSubscriberClass)
    }

    fun unregisterReactionSubscriber(reactionSubscriberClass: Any) {
        Fluxx.instance.unregisterReactionSubscriber(reactionSubscriberClass)
    }
}