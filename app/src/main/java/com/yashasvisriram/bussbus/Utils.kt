package com.yashasvisriram.bussbus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import java.util.*

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun <E> List<E>.getRandomElement() = this[Random().nextInt(this.size)]

fun padOrTruncateString(str: String, len: Int): String {
    return if (len <= str.length) {
        str.substring(0, len)
    } else {
        var paddedStr = str
        for (i in 0 until len - str.length) {
            paddedStr += " "
        }
        paddedStr
    }
}
