package com.github.hadywalied.ahramlockcontrolapp.ui

import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseActivity
import com.github.hadywalied.ahramlockcontrolapp.bluetoothStateLiveData
import com.github.hadywalied.ahramlockcontrolapp.locationStateLiveData
import com.github.hadywalied.ahramlockcontrolapp.tag
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber

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
        viewModel.unRegisterRecievers(this.application)
    }
}