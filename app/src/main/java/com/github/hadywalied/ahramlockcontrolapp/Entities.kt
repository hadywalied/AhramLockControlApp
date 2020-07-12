package com.github.hadywalied.ahramlockcontrolapp

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Devices(val device: BluetoothDevice, var rssi: Int) : Parcelable {
    override fun toString(): String = device.name + " " + device.address
}

data class Records(val name: String, val address: String, val time: Date)
