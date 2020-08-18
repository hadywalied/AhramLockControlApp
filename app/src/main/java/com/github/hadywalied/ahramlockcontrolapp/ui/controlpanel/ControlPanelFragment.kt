package com.github.hadywalied.ahramlockcontrolapp.ui.controlpanel


import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.api.load
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.UserType
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_control_panel.*
import kotlinx.android.synthetic.main.fragment_control_panel.toolbar
import kotlinx.android.synthetic.main.locker_layout.*
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import timber.log.Timber
import java.util.concurrent.TimeUnit


class ControlPanelFragment : BaseFragment() {

    //region variables
    private lateinit var viewModel: MainViewModel
    private val shareprefs by lazy {
        activity?.getSharedPreferences(getString(R.string.sharedprefsfile), Context.MODE_PRIVATE)
    }
    var delay: Int? = 0
    var device: Devices? = null
//endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.connectionStateLiveData?.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { showDisconnectedDialog(it.state) })

        viewModel.bleManagerRecievedData?.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { checkCommand(it) })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_control_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delay = shareprefs?.getInt(getString(R.string.delay_index), 6000)

        device = ControlPanelFragmentArgs.fromBundle(requireArguments()).connectedDevice
        when (ControlPanelFragmentArgs.fromBundle(requireArguments()).userType) {
            UserType.ADMIN -> {
                chip_group.visibility = View.VISIBLE
                fab_records.visibility = View.VISIBLE
            }
            UserType.USER -> {
                chip_group.visibility = View.INVISIBLE
                fab_records.visibility = View.INVISIBLE
            }
        }
        toolbar.title = device?.deviceName
        toolbar.subtitle = device?.address
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            viewModel.sendData(constructSendCommand("Disconnect"))
            viewModel.disconnect()
            findNavController().popBackStack()
        }
        btn_count.load(R.drawable.ic_lock) {
            crossfade(true)
            placeholder(R.drawable.ic_lock)
        }

        addDisposable(
            btn_count.clicks().throttleFirst(10000, TimeUnit.MILLISECONDS).subscribe {
//            btn_count.text = "unlocked"
                showImageViewGears()
                viewModel.sendData(constructSendCommand("UnLock", device?.address ?: ""))
                chip_group.isClickable = false
                fab_records.isEnabled = false
                tv_counter.text = "Unlocking, Please Wait"
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

    //region helper functions
    private fun checkCommand(s: String?) {
        val split: List<String> = s?.split("|")!!
        val delayDuration: Long = when (delay) {
            0 -> 3000
            1 -> 5000
            2 -> 8000
            else -> 6000
        }
        when (split.get(0)) {
            "UL" -> {
                btn_count.load(R.drawable.ic_check_circle) {
                    crossfade(true)
                    placeholder(R.drawable.ic_lock)
                }
                object : CountDownTimer(delayDuration, 100) {
                    override fun onTick(millisUntilFinished: Long) {
                        tv_counter?.text =
                            "seconds remaining till Locking: " + (millisUntilFinished / 1000).toString()
                    }

                    override fun onFinish() {
                        tv_counter?.text = "Locked!"
//                    btn_count.text = "UnLock"
                        chip_group?.isClickable = true
                        fab_records?.isEnabled = true
                        btn_count?.load(R.drawable.ic_lock) {
                            crossfade(true)
                            placeholder(R.drawable.ic_lock)
                        }
                    }
                }.start()
            }
            else -> {
                Timber.d(s)
            }
        }
    }

    private fun showDisconnectedDialog(it: ConnectionState.State) {
        val dialog =
            MaterialAlertDialogBuilder(requireContext()).setView(R.layout.info_connection_lost)
                .setCancelable(false)
                .setNeutralButton("Reconnect") { dialogInterface, _ ->
                    run {
                        val bluetoothDevice =
                            BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device?.address)
                        viewModel.connect(bluetoothDevice)
                        dialogInterface.dismiss()
                    }
                }.create()
        if (ConnectionState.State.CONNECTING == it || ConnectionState.State.READY == it) dialog.dismiss()
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
//endregion

    override fun onDetach() {
        super.onDetach()
        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.sendData(constructSendCommand("Disconnect"))
        viewModel.disconnect()
    }

}