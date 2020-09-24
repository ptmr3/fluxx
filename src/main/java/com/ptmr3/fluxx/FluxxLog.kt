package com.ptmr3.fluxx

class FluxxLog {
    private var mDebug: Boolean = false

    fun setDebug() {
        mDebug = true
    }

    fun print(message: String) {
        if (mDebug) { println(ROBUST_TAG + message) }
    }

    companion object {
        val instance: FluxxLog by lazy { FluxxLog() }
        private val TAG = "${FluxxLog::class.java.simpleName} | ${System.currentTimeMillis()}"
        private val ROBUST_TAG
            get() = Thread.currentThread().stackTrace[4]?.let { "$TAG: ${it.fileName}$${it.methodName}: " } ?: run { TAG }
    }
}