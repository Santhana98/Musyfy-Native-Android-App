package com.musyfy.nativeapp.core.util

import android.util.Log
import com.musyfy.nativeapp.BuildConfig

/**
 * Production-safe logging wrapper.
 * Ensures debug and verbose logs are only executed when BuildConfig.DEBUG is true.
 */
object AppLog {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }
}
