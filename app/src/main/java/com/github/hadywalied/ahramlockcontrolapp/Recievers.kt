package com.github.hadywalied.ahramlockcontrolapp

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import timber.log.Timber


/**
 * these are a LiveData components responsible for sending the signal
 * when location services and bluetooth services is lost
 */
val bluetoothStateLiveData = MutableLiveData(true)
val locationStateLiveData = MutableLiveData(true)

/**
 * this is a location broadcast receiver to send a signal when location services is lost
 */
val MyLocationBroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        locationStateLiveData.postValue(isLocationAvailable(context))
    }
}


/**
 * this is a location broadcast receiver to send a signal when the Bluetooth service is lost
 */
val MyBluetoothBroadcastReceiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Receiver Bluetooth Received")
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

        when (state) {
            BluetoothAdapter.STATE_ON -> {
                Timber.d("${bluetoothStateLiveData.value}")
                bluetoothStateLiveData.postValue(true)
                Timber.d("${bluetoothStateLiveData.value}")
            }
            BluetoothAdapter.STATE_OFF -> {
                Timber.d("${bluetoothStateLiveData.value}")
                bluetoothStateLiveData.postValue(false)
                Timber.d("${bluetoothStateLiveData.value}")
            }
        }
    }
}
