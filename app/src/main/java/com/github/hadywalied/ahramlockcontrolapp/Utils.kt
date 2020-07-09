package com.github.hadywalied.ahramlockcontrolapp

import java.lang.StringBuilder

fun ByteArray.toHex(): String {
    val stringbuilder = StringBuilder()
    forEach { byte -> stringbuilder.append(byte.toString(16)) }
    return stringbuilder.toString()
}
