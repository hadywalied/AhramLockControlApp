package com.github.hadywalied.ahramlockcontrolapp

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Devices(val device: BluetoothDevice, var rssi: Int) : Parcelable {
    override fun toString(): String = device.name + " " + device.address
}

