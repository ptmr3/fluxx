package com.ptmr3.fluxx.test

import com.ptmr3.fluxx.FluxxAction
import com.ptmr3.fluxx.FluxxReaction

class FluxxTest {
    fun getFluxxAction(type: String, data: HashMap<String, Any>? = null) = FluxxAction(type, data)

    fun getFluxxReaction(type: String, data: HashMap<String, Any>? = null) = FluxxReaction(type, data)
}