package com.yashasvisriram.bussbus


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
