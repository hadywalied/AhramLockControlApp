package com.github.hadywalied.ahramlockcontrolapp

import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalDateTime

// IGNORE This One
fun ByteArray.toHex(): String {
    val stringbuilder = StringBuilder()
    forEach { byte -> stringbuilder.append(byte.toString(16)) }
    return stringbuilder.toString()
}


/**
 * @param opCode is the OpCode that is being transmitted
 * @param arguments is the inputs to be sent to the bluetooth i.e. the user number and/or MAC address
 **/
fun constructSendCommand(opCode: String, vararg arguments: String): String {
    return when (opCode) {
        "Connect" -> {
            "C" + "|" + arguments[0]
        }
        "AddUser" -> {
            "AU" + "|" + arguments[1]
        }
        "RmUser" -> {
            "RU" + "|" + arguments[0]
        }
        "Lock" -> {
            "L" + "|" + arguments[0]
        }
        "UnLock" -> {
            "UL" + "|" + arguments[0]
        }
        "GetRecords" -> {
            "R|" + arguments[0] + "|" + arguments[1]
        }
        "GetUsers" -> {
            "U|" + arguments[0]
        }
        "ToggleOperationMode" -> {
            "OP"
        }
        "Sync" -> {
            "T" + "|" + arguments[0]
        }
        else -> {
            "E"
        }
    }
}

fun getTimeDate(customFormat: String) =
    "20${customFormat[0]}${customFormat[1]}" +
            "-${customFormat[2]}${customFormat[3]}-" +
            "${customFormat[4]}${customFormat[5]}" + "T${customFormat[6]}${customFormat[7]}:" +
            "${customFormat[8]}${customFormat[9]}:" + "${customFormat[10]}${customFormat[11]}"

