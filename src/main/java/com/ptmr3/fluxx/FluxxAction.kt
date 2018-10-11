package com.ptmr3.fluxx

data class FluxxAction internal constructor(val type: String, val data: HashMap<String, Any>? = null) {
    operator fun <T> get(tag: String) = data?.get(tag) as T

    override fun toString() = "${this.javaClass.simpleName} { Type: $type, Data: $data }"
}