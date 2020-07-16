package com.github.hadywalied.ahramlockcontrolapp

import android.bluetooth.BluetoothDevice
import android.icu.util.LocaleData
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Parcelize
data class Devices(val device: BluetoothDevice, var rssi: Int) : Parcelable {
    override fun toString(): String = device.name + " " + device.address
}

data class Records(val name: String, val address: String, val localDateTime: LocalDateTime) {
    companion object{
        fun fromFormattedString(stringRecord: String): Records {
            val str = stringRecord.split("$", ignoreCase = true)
//        Example String For Input 2018-01-28T13:42:17.546
            val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.parse(str[2])
            } else {
                TODO("VERSION.SDK_INT < O")

            }
            return Records(str[0], str[1], date)
        }
    }
    override fun toString(): String {
        return "$name$$address$$localDateTime"
    }
}
