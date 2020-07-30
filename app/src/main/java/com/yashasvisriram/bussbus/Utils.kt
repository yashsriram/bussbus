package com.yashasvisriram.bussbus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import java.lang.StringBuilder

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun String.padOrTruncateString(len: Int): String {
    return if (len <= length) {
        substring(0, len)
    } else {
        val paddedStr = StringBuilder(this)
        for (i in 0 until len - length) {
            paddedStr.append(' ')
        }
        paddedStr.toString()
    }
}
