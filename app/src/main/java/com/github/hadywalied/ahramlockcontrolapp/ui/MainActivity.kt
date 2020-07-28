package com.github.hadywalied.ahramlockcontrolapp.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.hadywalied.ahramlockcontrolapp.*
import com.github.hadywalied.ahramlockcontrolapp.base.BaseActivity
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.info_no_bluetooth.*
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity() {

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Timber.plant(Timber.DebugTree())
        Timber.tag(tag)
        Timber.d("App Created")
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        bluetoothStateLiveData.observe(this, Observer { updateBluetoothUi(it) })
        locationStateLiveData.observe(this, Observer { updateLocationUi(it) })
    }

    private fun updateBluetoothUi(b: Boolean?) {
        if (!b!!) {
            layout_devices_fragment.visibility = View.GONE
            layout_no_bluetooth.visibility = View.VISIBLE
            addDisposable(
                action_enable_bluetooth.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS)
                    .subscribe {
                        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            startActivityForResult(
                                enableBtIntent,
                                REQUEST_ENABLE_BT
                            )
                        }
                    })
        } else {
            layout_devices_fragment.visibility = View.VISIBLE
            layout_no_bluetooth.visibility = View.GONE
        }
    }

    private fun updateLocationUi(b: Boolean?) {

    }

    override fun onStart() {
        super.onStart()
        Timber.d("Receivers Registered")
        viewModel.registerBroadcastRecievers(this.application)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Receivers Un Registered")
        viewModel.unRegisterReceivers(this.application)
    }
}