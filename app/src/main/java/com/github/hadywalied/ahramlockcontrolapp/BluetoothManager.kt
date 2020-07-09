package com.github.hadywalied.ahramlockcontrolapp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager

class MyBleManager(context: Context) : ObservableBleManager(context) {
    override fun getGattCallback(): BleManagerGattCallback {
        return MyBleManagerGattCallback()
    }

    private inner class MyBleManagerGattCallback : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
        }

        override fun initialize() {
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            return false
        }

    }


}

abstract class MyCallback : ProfileDataCallback, CallbackInterface, DataSentCallback {
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() < 1) {
            onInvalidDataReceived(device, data)
            return
        }
        val received = data.getStringValue(Data.FORMAT_UINT32)
        onData(device, received ?: "")
    }

    override fun onDataSent(device: BluetoothDevice, data: Data) {
        if (data.size() < 1) {
            onInvalidDataReceived(device, data)
            return
        }
        val received = data.getStringValue(Data.FORMAT_UINT32)
        onData(device, received ?: "")

    }
}

interface CallbackInterface {
    fun onData(device: BluetoothDevice, data: String)
}