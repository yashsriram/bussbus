package io.buggedbit.bussbus

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
