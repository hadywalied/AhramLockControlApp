package com.github.hadywalied.ahramlockcontrolapp

import android.bluetooth.BluetoothDevice
import android.icu.util.LocaleData
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Parcelize
@Entity(tableName = "devices")
data class Devices(
    @PrimaryKey val address: String = "",
    val deviceName: String? = "UnNamed",
    var rssi: Int = 100
) :
    Parcelable {
    override fun toString(): String = "$deviceName $address"
}

@Entity(tableName = "records")
@Parcelize
data class Records(
    @PrimaryKey val address: String,
    val name: String = "",
    val localDateTime: String = ""
) : Parcelable

@Parcelize
data class Users(
    val id: String,
    val name: String
    , val address: String
) : Parcelable
