package com.github.hadywalied.ahramlockcontrolapp

import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.StringBuilder

// IGNORE This One
fun ByteArray.toHex(): String {
    val stringbuilder = StringBuilder()
    forEach { byte -> stringbuilder.append(byte.toString(16)) }
    return stringbuilder.toString()
}

fun processRecievedCommand(recieved: String): String {
    val recievedArr = recieved.split(",", ignoreCase = true)
    return when (recievedArr[0]) {
        "Act" -> {
            recievedArr[1] // 0 or 1 [true or false]
        }
        "Res$1" -> {
            ""
        }
        "Res$2" -> {
            ""
        }
        else -> {
            "E"
        }
    }
}

/**
 * @param opCode is the OpCode that is being transmitted
 * @param arguments is the inputs to be sent to the bluetooth
 **/
fun constructSendCommand(opCode: String, vararg arguments: String): String {
    return when (opCode) {
        "Connect" -> {
            /**
             * @param arguments is a list of strings containing the elements of the arguments
             * @param arguments[0] is the user's MAC address
             */
            opCode + "," + arguments[0]
        }
        "AddUser" -> {
            /**
             * @param arguments is a list of strings containing the elements of the arguments
             * @param arguments[0] is the first element which is the username
             * @param arguments[1] is the second element which is the user's MAC address
             */
            opCode + "," + arguments[0] + "," + arguments[1]
        }
        "RmUser" -> {
            /**
             * @param arguments is a list of strings containing the elements of the arguments
             * @param arguments[0] is the user's MAC address
             */
            opCode + "," + arguments[0]
        }
        "Lock" -> {
            /**
             * @param arguments is a list of strings containing the elements of the arguments
             * @param arguments[0] is the user's MAC address
             */
            opCode + "," + arguments[0]
        }
        "UnLock" -> {
            /**
             * @param arguments is a list of strings containing the elements of the arguments
             * @param arguments[0] is the user's MAC address
             */
            opCode + "," + arguments[0]
        }
        "GetRecords" -> {
            opCode
        }
        "Sync" -> {
            opCode + "," + arguments[1]
        }
        else -> {
            "E"
        }
    }

}