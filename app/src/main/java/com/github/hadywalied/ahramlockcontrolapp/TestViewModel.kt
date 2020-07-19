package com.github.hadywalied.ahramlockcontrolapp

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.support.v18.scanner.*
import timber.log.Timber

class MainViewModel(app: Application) : AndroidViewModel(app) {

    //region date links
    val devicesItems = arrayListOf<Devices>()

    private val _allBluetoothDevicesLiveData = MutableLiveData<List<Devices>>()
    val allBluetoothDevicesLiveData: LiveData<List<Devices>>
        get() = _allBluetoothDevicesLiveData

    private val _scanFailedLiveData = MutableLiveData(false)
    val scanFailedLiveData: LiveData<Boolean>
        get() = _scanFailedLiveData

    private val _loadingLiveData = MutableLiveData(false)
    val loadingLiveData: LiveData<Boolean>
        get() = _loadingLiveData


    private val myBleManager = MyBleManager(app)

    val managerLiveData = myBleManager.liveData

    //endregion

    // region Bluetooth Functions
    fun scan() {
        _loadingLiveData.postValue(true)
        devicesItems.clear()

        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings =
            ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setUseHardwareBatchingIfSupported(false)
                .build()
        val filters: MutableList<ScanFilter> = ArrayList()

//        with(filters) {
//            add(ScanFilter.Builder().setServiceUuid(SCAN_SERVICE_UUID).build())
//        }

        val scanCallback = object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                _scanFailedLiveData.postValue(true)
                super.onScanFailed(errorCode)
            }

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                var bool = false
                for (dev in devicesItems) {
                    if (dev.device.address == result.device.address) {
                        dev.rssi = result.rssi
                        bool = true
                    }
                }
                if (!bool) {
                    devicesItems.add(Devices(result.device, result.rssi))
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
        }, 7500)
    }

    fun connect(device: Devices) {
        myBleManager.connect(device.device)
            .useAutoConnect(true)
            .retry(3, 100)
            .enqueue()
    }

    fun disconnect() {
        myBleManager.disconnect().enqueue()
    }

    fun sendData(string: String) {
        if (myBleManager.isConnected) {
            myBleManager.sendData(string)
        }
    }
//endregion

    override fun onCleared() {
        super.onCleared()
        if (myBleManager.isConnected) {
            disconnect()
        }
    }

}
