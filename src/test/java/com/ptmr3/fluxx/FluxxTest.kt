package com.ptmr3.fluxx

class FluxxTest {
    fun getFluxxAction(type: String, data: HashMap<String, Any>? = null) = FluxxAction(type, data)

    fun getFluxxReaction(type: String, data: HashMap<String, Any>? = null) = FluxxReaction(type, data)
}