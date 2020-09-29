package com.github.hadywalied.ahramlockcontrolapp.ui

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    //region date links

    /**
     *  the devices list lists up the scanned devices
     *  @sample BluetoothDevice
     *  and the devices map is used to map the to the address for quick access.
     */
    val devicesItems = arrayListOf<Devices>()
    val devicesSet = mutableMapOf<String, BluetoothDevice>()

    //this handles sending the devices list to the UI
    private val _allBluetoothDevicesLiveData = MutableLiveData<List<Devices>>()
    val allBluetoothDevicesLiveData: LiveData<List<Devices>>
        get() = _allBluetoothDevicesLiveData

    private val _scanFailedLiveData = MutableLiveData(false)
    val scanFailedLiveData: LiveData<Boolean>
        get() = _scanFailedLiveData

    private val _connectFailedLiveData = MutableLiveData(false)
    val connectFailedLiveData: LiveData<Boolean>
        get() = _connectFailedLiveData

    //this handles the UI Loading State Management
    private val _loadingLiveData = MutableLiveData(false)
    val loadingLiveData: LiveData<Boolean>
        get() = _loadingLiveData

    //the Custom BLE Manger is a singleton class btw.
    val myBleManager = getInstance(app)

    /**
     * @see bleManagerRecievedData
     * THIS VARIABLE HANDLES INCOMMING DATA AND SENDS IT TO UI
     */
    val bleManagerRecievedData = myBleManager?.receievedLiveData

    // Updates the UI with the connection state
    private val _connectionStateLiveData = myBleManager?.state
    val connectionStateLiveData: LiveData<ConnectionState>?
        get() = _connectionStateLiveData
    //endregion

    // region Bluetooth Functions

    //region scaners
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

    /*fun scanUsers() {
        _loadingLiveData.postValue(true)
        devicesItems.clear()
        devicesSet.clear()
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings =
            ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setUseHardwareBatchingIfSupported(true)
                .build();

        val filters: MutableList<ScanFilter> = ArrayList()

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
        }, 3000)
    }*/
    fun scanUsers() {
        _loadingLiveData.postValue(true)
        devicesItems.clear()
        devicesSet.clear()
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

        // Create a BroadcastReceiver for ACTION_FOUND.
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        var bool = false
                        for (dev in devicesItems) {
                            if (dev.address == device?.address) {
                                dev.rssi = 70
                                bool = true
                            }
                        }
                        if (!bool) {
                            devicesItems.add(
                                Devices(
                                    device?.address!!,
                                    device.name,
                                    70
                                )
                            )
                            devicesSet[device.address!!] = device
                        }
                        _allBluetoothDevicesLiveData.postValue(devicesItems)
                        _scanFailedLiveData.postValue(false)
                    }
                }
            }
        }
        app.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()

        Handler(Looper.getMainLooper()).postDelayed({app.unregisterReceiver(receiver)},2000)
    }
    //endregion

    //region connection handling
    /**
     * the connect function handles the connection to the BLE Device, notice that it takes a
     * @param device that is the actual physical BLE device that is scanned
     */
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
//endregion

    /**
     * the sendData function sends a string to the BLE device after checking that it's connected.
     */
    fun sendData(string: String) {
        if (myBleManager?.isConnected == true) {
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

    fun unRegisterReceivers(app: Application) {
        app.unregisterReceiver(MyBluetoothBroadcastReceiver)
        app.unregisterReceiver(MyLocationBroadcastReceiver)
    }
    //endregion

}
