package com.zxhy.android.greenscreen.base.util

import android.content.Context
import android.util.Log

/**
 *@author 周智慧
 *2022/12/6 16:34
 *@description:
 **/
object LogUtil {
    private const val TAG_PRE = ":zzh"


    private fun buildTag(tag: String?): String {
        return tag + TAG_PRE;
    }

    fun log(any: Any?) {
        Log.i(TAG_PRE, any?.toString() ?: "null")
    }

    fun v(tag: String?, msg: String) {
        Log.v(buildTag(tag), msg)
    }

    fun d(tag: String?, msg: String) {
        Log.d(buildTag(tag), msg)
    }

    fun i(tag: String?, msg: String) {
        Log.i(buildTag(tag), msg)
    }

    fun w(tag: String?, msg: String) {
        Log.w(buildTag(tag), msg)
    }

    fun w(tag: String?, msg: String, tr: Throwable? = null) {
        Log.w(buildTag(tag), msg, tr)
    }

    fun e(tag: String?, msg: String) {
        Log.e(buildTag(tag), msg)
    }

    fun e(tag: String?, tr: Throwable? = null) {
        this.e(tag, "", tr)
    }
    fun e(tag: String?, msg: String, tr: Throwable? = null) {
        Log.e(buildTag(tag), msg, tr)
    }

}