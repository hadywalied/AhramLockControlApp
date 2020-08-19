package com.github.hadywalied.ahramlockcontrolapp

import android.content.Context
import android.provider.Settings
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

enum class UserType {
    ADMIN, USER
}

/**
 * @param opCode is the OpCode that is being transmitted
 * @param arguments is the inputs to be sent to the bluetooth i.e. the user number and/or MAC address
 **/
fun constructSendCommand(opCode: String, vararg arguments: String): String {
    var s = when (opCode) {
        "Connect" -> {
            "C" + "|" + arguments[0] + "#"
        }
        "Setup" -> {
            "S" + "|" + arguments[0] + "|" + arguments[1] + "#"
        }
        "Disconnect" -> {
            "DC" + "#"
        }
        "AddUser" -> {
            "AUM" + "|" + arguments[0] + "#"
        }
        "AddNFC" -> {
            "AUC" + "#"
        }
        "CancelAddingUser" -> {
            "CAUM" + "#"
        }
        "CancelAddingNFC" -> {
            "CAUC" + "#"
        }
        "RmUser" -> {
            "DU" + "|" + arguments[0] + "#"
        }
        "GetUsers" -> {
            "W" + "#"
        }
        "CancelGetUsers" -> {
            "WE" + "#"
        }
        "UnLock" -> {
            "UL" + "#"
        }
        "GetAllRecords" -> {
            "R" + "#"
        }
        "UpdateRecords" -> {
            "RS" + "#"
        }
        "CancelRecords" -> {
            "RC" + "#"
        }
        "Sync" -> {
            "T" + "|" + arguments[0] + "#"
        }
        "SetUnlockDelay" -> {
            "LT" + "|" + arguments[0] + "#"
        }
        "GetBattery" -> {
            "B" + "#"
        }
        "GetDelay" -> {
            "LT" + "#"
        }
        else -> {
            "E"
        }
    }
    while (s.length <= 20) {
        s += "*"
    }
    return s
}

// format is Time: HH:mm:ss
//           Date: dd-MM-YY
fun getTimeDate(customFormat: String) =
    "Time: ${customFormat[0]}${customFormat[1]}:" +
            "${customFormat[2]}${customFormat[3]}:" +
            "${customFormat[4]}${customFormat[5]}" + "\nDate: ${customFormat[6]}${customFormat[7]}-" +
            "${customFormat[8]}${customFormat[9]}-" + "20${customFormat[10]}${customFormat[11]}"

//this is a helper function to calculate the time and puts it into the required format that is HHmmssddMMYYYY
fun getCurrentTimeDate() =
    SimpleDateFormat("HHmmssddMMYYYY", Locale.US).format(Calendar.getInstance().time)
        .toString()

//checks if the location services is available or not.
fun isLocationAvailable(context: Context): Boolean {
    var locationMode = Settings.Secure.LOCATION_MODE_OFF
    try {
        locationMode = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.LOCATION_MODE
        )
    } catch (e: Settings.SettingNotFoundException) {
    }
    return locationMode != Settings.Secure.LOCATION_MODE_OFF
}