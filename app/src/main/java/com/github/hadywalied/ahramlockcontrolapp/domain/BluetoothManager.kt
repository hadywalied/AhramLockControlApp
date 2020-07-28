package com.github.hadywalied.ahramlockcontrolapp.domain

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import timber.log.Timber
import java.util.*


class MyBleManager(context: Context) : ObservableBleManager(context) {

    companion object {
        @Volatile
        private var instance: MyBleManager? = null
        fun getInstance(context: Context): MyBleManager? {
            return instance ?: synchronized(MyBleManager::class.java) {
                if (instance == null) {
                    instance = MyBleManager(context)
                }
                return instance
            }
        }
    }

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
            Timber.e("onInvalidDataReceived: data")
        }

        override fun fromOnDataSent(device: BluetoothDevice, data: String) {
            Timber.d(device.name + " " + device.address + " " + data)
            _mutableLiveData.postValue(data)
        }
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return MyBleManagerGattCallback()
    }

    fun sendData(string: String) {
        if (characteristic != null) {
            //TODO Handle SENDING States
            writeCharacteristic(characteristic, string.toByteArray()).with(callback).enqueue()
        }
    }

    override fun shouldClearCacheWhenDisconnected(): Boolean {
        return !supported
    }

    private inner class MyBleManagerGattCallback : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            characteristic = null
            Timber.d("onDeviceDisconnected: Disconnected")
        }

        override fun initialize() {
            Timber.d("initialize: ")

            setNotificationCallback(characteristic).with(callback)
            readCharacteristic(characteristic).with(callback).enqueue()

            enableNotifications(characteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            Timber.d("isRequiredServiceSupported: ")
            val service = gatt.getService(SERVICEUUID)
            Timber.d("isRequiredServiceSupported: $service")
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

abstract class MyCallback : ProfileDataCallback,
    CallbackInterface, DataSentCallback {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() < 1) {
            onInvalidDataReceived(device, data)
            return
        }
        val received = data.getStringValue(0)
        fromOnDataSent(device, received ?: "")
    }

    override fun onDataSent(device: BluetoothDevice, data: Data) {
        if (data.size() < 1) {
            onInvalidDataReceived(device, data)
            return
        }
        val received = data.getStringValue(0)
        fromOnDataSent(device, received ?: "")
    }
}

interface CallbackInterface {
    fun fromOnDataSent(device: BluetoothDevice, data: String)
}

//region Main Ble Manager
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
//endregion