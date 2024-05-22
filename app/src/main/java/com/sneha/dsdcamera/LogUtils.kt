package com.sneha.dsdcamera

import android.util.Log

object LogUtils {

    fun String.logDebug(tag: String) {
        Log.d(tag, this)
    }

    fun tryAndCatchLog(tag: String, run: () -> Unit) {
        try {
            run()
        } catch (e: Exception) {
            e.logError(tag)
        }
    }


    fun String.logError(){
        Log.e("HandledError", this)
    }

    fun Exception.logError(tag: String) {
        Log.e(tag, this.getErrorMessage())
    }

    private fun Exception.getErrorMessage(): String {
        return """
            |type = ${this::class.simpleName}
            |message = ${this.message}
            |stack trace = ${this.stackTraceToString()}
        """.trimMargin()
    }
}