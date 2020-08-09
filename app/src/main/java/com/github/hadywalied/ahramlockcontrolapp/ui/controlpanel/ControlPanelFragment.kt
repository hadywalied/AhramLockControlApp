package com.github.hadywalied.ahramlockcontrolapp.ui.controlpanel


import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.api.load
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_control_panel.*
import kotlinx.android.synthetic.main.fragment_control_panel.toolbar
import kotlinx.android.synthetic.main.locker_layout.*
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import java.util.concurrent.TimeUnit


class ControlPanelFragment : BaseFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.connectionStateLiveData?.observe(
            viewLifecycleOwner,
            Observer { showDisconnectedDialog(it.state) })
        return inflater.inflate(R.layout.fragment_control_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val delay: Long = 6000
        val device = ControlPanelFragmentArgs.fromBundle(requireArguments()).connectedDevice
        toolbar.title = device.deviceName
        toolbar.subtitle = device.address
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            viewModel.disconnect()
            findNavController().navigateUp()
            findNavController().popBackStack()
        }
        btn_count.load(R.drawable.ic_lock){
            crossfade(true)
            placeholder(R.drawable.ic_lock)
        }

        addDisposable(btn_count.clicks().throttleFirst(delay, TimeUnit.MILLISECONDS).subscribe {
//            btn_count.text = "unlocked"
            showImageViewGears()
            viewModel.sendData(constructSendCommand("UnLock", device.address))
            object : CountDownTimer(delay, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    tv_counter.text =
                        "seconds remaining till Locking: " + (millisUntilFinished / 1000).toString()
                }

                override fun onFinish() {
                    tv_counter.text = "Locked!"
//                    btn_count.text = "UnLock"
                    viewModel.sendData(constructSendCommand("Lock", device.address))
                    btn_count.load(R.drawable.ic_lock){
                        crossfade(true)
                        placeholder(R.drawable.ic_lock)
                    }
                }
            }.start()
        })

        addDisposable(
            fab_records.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                findNavController().navigate(R.id.action_controlPanelFragment_to_recordsFragment)
            })
        addDisposable(chip_basic.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_controlPanelFragment_to_basicInfoFragment)
        })
        addDisposable(
            chip_admins.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                findNavController().navigate(R.id.action_controlPanelFragment_to_usersFragment)
            })

    }

    private fun showDisconnectedDialog(it: ConnectionState.State) {
        val dialog =
            MaterialAlertDialogBuilder(requireContext()).setView(R.layout.info_connection_lost)
                .setCancelable(false)
                .setNeutralButton("Reconnect") { dialogInterface, _ ->
                    run {
                        findNavController().navigateUp()
                        findNavController().popBackStack()
                        dialogInterface.dismiss()
                    }
                }.create()
        if (ConnectionState.State.CONNECTING == it || ConnectionState.State.INITIALIZING == it) dialog.dismiss()
        else if (ConnectionState.State.DISCONNECTED == it) dialog.show()
    }

    private fun showImageViewGears() {
        val imageLoader = ImageLoader.Builder(requireContext())
            .componentRegistry {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder())
                } else {
                    add(GifDecoder())
                }
            }
            .build()
        btn_count.load(R.drawable.gear_duo, imageLoader = imageLoader) {
            crossfade(true)
            placeholder(R.drawable.ic_lock)
        }
    }

}