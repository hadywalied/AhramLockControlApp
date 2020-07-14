package com.github.hadywalied.ahramlockcontrolapp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import java.util.*


class MyBleManager(context: Context) : ObservableBleManager(context) {

    val SERVICEUUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
    val CHARUUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")

    private val _mutableLiveData = MutableLiveData<String>()
    val liveData: LiveData<String>
        get() = _mutableLiveData

    private var characteristic: BluetoothGattCharacteristic? = null

    var supported = false

    private val callback = object : MyCallback() {
        override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
            super.onInvalidDataReceived(device, data)
            Log.e(tag, "onInvalidDataReceived: data")
        }

        override fun onData(device: BluetoothDevice, data: String) {
            _mutableLiveData.postValue(data)
        }
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return MyBleManagerGattCallback()
    }

    fun sendData(string: String) {
        if (characteristic != null) {
            writeCharacteristic(characteristic, string.toByteArray()).with(callback).enqueue()
        }
    }

    private inner class MyBleManagerGattCallback : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            characteristic = null
            Log.d(tag, "onDeviceDisconnected: Disconnected")
        }

        override fun initialize() {
            Log.d(tag, "initialize: ")

            setNotificationCallback(characteristic)
            readCharacteristic(characteristic)
            waitForWrite(characteristic)
            //TODO make a new characteristic to read and another one to write....
            enableNotifications(characteristic)
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            Log.d(tag, "isRequiredServiceSupported: ")

            val service = gatt.getService(SERVICEUUID)
            Log.d(tag, "isRequiredServiceSupported: $service")
            if (service != null) {
                characteristic = service.getCharacteristic(CHARUUID)
            }
            var writeRequest = false
            if (characteristic != null) {
                val rxProperties = characteristic!!.properties
                writeRequest = (BluetoothGattCharacteristic.PROPERTY_WRITE xor rxProperties) > 0
            }
            supported = characteristic != null && writeRequest
            return supported
        }

    }

}

class MainBleManager(context: Context) : ObservableBleManager(context) {
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