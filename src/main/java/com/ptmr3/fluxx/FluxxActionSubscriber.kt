package com.ptmr3.fluxx

interface FluxxActionSubscriber {
    fun registerActionSubscriber(actionSubscriberClass: Any) {
        Fluxx.sInstance!!.registerReactionSubscriber(actionSubscriberClass)
    }
}