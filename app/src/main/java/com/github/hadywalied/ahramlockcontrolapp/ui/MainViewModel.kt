package com.github.hadywalied.ahramlockcontrolapp.ui

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.MyBluetoothBroadcastReceiver
import com.github.hadywalied.ahramlockcontrolapp.MyLocationBroadcastReceiver
import com.github.hadywalied.ahramlockcontrolapp.SCAN_SERVICE_UUID
import com.github.hadywalied.ahramlockcontrolapp.domain.MyBleManager.Companion.getInstance
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import no.nordicsemi.android.support.v18.scanner.*
import timber.log.Timber

class MainViewModel(app: Application) : AndroidViewModel(app) {

    //region date links
    val devicesItems = arrayListOf<Devices>()
    val devicesSet = mutableMapOf<String, BluetoothDevice>()

    private val _allBluetoothDevicesLiveData = MutableLiveData<List<Devices>>()
    val allBluetoothDevicesLiveData: LiveData<List<Devices>>
        get() = _allBluetoothDevicesLiveData

    private val _scanFailedLiveData = MutableLiveData(false)
    val scanFailedLiveData: LiveData<Boolean>
        get() = _scanFailedLiveData

    private val _connectFailedLiveData = MutableLiveData(false)
    val connectFailedLiveData: LiveData<Boolean>
        get() = _connectFailedLiveData


    private val _loadingLiveData = MutableLiveData(false)
    val loadingLiveData: LiveData<Boolean>
        get() = _loadingLiveData


    val myBleManager = getInstance(app)

    val bleManagerRecievedData = myBleManager?.receievedLiveData
    val bleManagerConnectionState = myBleManager?.connectedLiveData

    private val _connectionStateLiveData = myBleManager?.state
    val connectionStateLiveData: LiveData<ConnectionState>?
        get() = _connectionStateLiveData
    //endregion

    // region Bluetooth Functions
    fun scan() {
        _loadingLiveData.postValue(true)
        devicesItems.clear()
        devicesSet.clear()

        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings =
            ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setUseHardwareBatchingIfSupported(false)
                .build()
        val filters: MutableList<ScanFilter> = ArrayList()

        //TODO Remove If Requested
        with(filters) {
            add(ScanFilter.Builder().setServiceUuid(SCAN_SERVICE_UUID).build())
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                _scanFailedLiveData.postValue(true)
                super.onScanFailed(errorCode)
            }

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                var bool = false
                for (dev in devicesItems) {
                    if (dev.address == result.device.address) {
                        dev.rssi = result.rssi
                        bool = true
                    }
                }
                if (!bool) {
                    devicesItems.add(
                        Devices(
                            result.device.address,
                            result.device.name,
                            result.rssi
                        )
                    )
                    devicesSet[result.device.address] = result.device
                }
                _allBluetoothDevicesLiveData.postValue(devicesItems)
                Timber.d("onScanResult() returned: $result")
                _scanFailedLiveData.postValue(false)
                super.onScanResult(callbackType, result)
            }
        }
        scanner.startScan(filters, settings, scanCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(scanCallback)
            _loadingLiveData.postValue(false)
        }, 3750)
    }

    fun connect(device: BluetoothDevice) {
        Timber.d("Connect")
        with(myBleManager!!) {
            connect(device).run {
                useAutoConnect(true)
                retry(3, 100)
                before { _loadingLiveData.postValue(true) }
                done {
                    Timber.d("Connected Successfully")
                    _connectFailedLiveData.postValue(false)
                    _loadingLiveData.postValue(false)
                }
                fail { _, _ ->
                    run {
                        Timber.d("Connection Failed")
                        _connectFailedLiveData.postValue(true)
                        _loadingLiveData.postValue(false)
                    }
                }
                enqueue()
            }
        }

    }

    fun disconnect() {
        myBleManager?.disconnect()?.enqueue()
    }

    fun sendData(string: String) {
        if (myBleManager?.isConnected!!) {
            myBleManager.sendData(string)
        }
    }
//endregion

    //region broadcast receivers
    fun registerBroadcastRecievers(app: Application) {
        app.registerReceiver(
            MyBluetoothBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        app.registerReceiver(
            MyLocationBroadcastReceiver,
            IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        )
    }
    //endregion

    override fun onCleared() {
        super.onCleared()
        if (myBleManager?.isConnected!!) {
            disconnect()
        }
    }

    fun unRegisterReceivers(app: Application) {
        app.unregisterReceiver(MyBluetoothBroadcastReceiver)
        app.unregisterReceiver(MyLocationBroadcastReceiver)
    }

}
