package com.github.hadywalied.ahramlockcontrolapp.ui.mydevices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.*
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_user_devices.toolbar
import kotlinx.android.synthetic.main.recycler_layout.*
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import androidx.appcompat.app.AlertDialog as AlertDialog

class SavedDevicesFragment : Fragment() {


    //region variables
    private lateinit var viewModel: MainViewModel
    private var alertDialog: AlertDialog? = null

    private lateinit var repo: DevicesRepo

    private val shareprefs by lazy {
        activity?.getSharedPreferences(getString(R.string.sharedprefsfile), Context.MODE_PRIVATE)
    }
//endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_user_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            viewModel.sendData(constructSendCommand("Disconnect"))
            viewModel.disconnect()
            findNavController().popBackStack()
        }
        updateRecyclerList()
        swipe.setOnRefreshListener {
            updateRecyclerList()
            Handler(Looper.getMainLooper()).postDelayed({ swipe?.isRefreshing = false }, 1500)
        }

    }

    //region helper functions
    private fun checkCommand(s: String?) {
        val split: List<String> = s?.split("|")!!
        when (split.get(0)) {
            "C" -> {
                alertDialog?.cancel()
                when (split[1]) {
                    "0" -> {
                        viewModel.sendData(constructSendCommand("Sync", getCurrentTimeDate()))
                        navigateWhenConnected(UserType.ADMIN)
                    }
                    "1" -> {
                        navigateWhenConnected(UserType.USER)
                    }
                    "F" -> {
                        Toast.makeText(requireContext(), "Connection Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun handleConnectionState(state: ConnectionState.State) {
        when (state) {
            ConnectionState.State.READY -> {
                sendConnectCommand()
            }
            ConnectionState.State.CONNECTING -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (BluetoothGatt.STATE_CONNECTING == viewModel.myBleManager?.connectionState) {
                        Toast.makeText(
                            requireActivity(),
                            "Connection Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog!!.dismiss()
                        viewModel.sendData(constructSendCommand("Disconnect"))
                        viewModel.disconnect()
                    }
                }, 3000)
            }
            ConnectionState.State.DISCONNECTED -> {
                alertDialog?.dismiss()
            }
//            ConnectionState.State.INITIALIZING -> sendConnectCommand()
        }
    }

    private fun sendConnectCommand() {
        val arguments =
            viewModel.myBleManager?.bluetoothDevice?.address?.filter { it.isLetterOrDigit() }
                ?: ""
        viewModel.sendData(
            constructSendCommand(
                "Connect",
                arguments
            )
        )
    }

    private fun showConnectingDialog(): AlertDialog {
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleLarge)
        progressBar.isIndeterminate = true
        progressBar.progressTintList = ColorStateList.valueOf(Color.YELLOW);
        val alertDialog = MaterialAlertDialogBuilder(requireContext()).setTitle("Connecting")
            .setBackground(
                resources.getDrawable(
                    R.color.primaryTextColor,
                    resources.newTheme()
                )
            )
            .setNegativeButton("Cancel") { dialogInterface, i ->
                dialogInterface.dismiss()
                viewModel.sendData(constructSendCommand("Disconnect"))
                viewModel.disconnect()
            }
            .setCancelable(false)
            .setView(progressBar).create()
        alertDialog.show()
        return alertDialog
    }

    private fun navigateWhenConnected(type: UserType) {
        alertDialog?.dismiss()
        val devices = Devices(
            viewModel.myBleManager?.bluetoothDevice?.address ?: "",
            viewModel.myBleManager?.bluetoothDevice?.name ?: "",
            0
        )
        SavedDevicesFragmentDirections.actionUserDevicesFragmentToControlPanelFragment(
            devices,
            type
        )
            .let { action ->
                findNavController().navigate(action.actionId, action.arguments)
            }
        with(shareprefs?.edit()) {
            this?.putString(getString(R.string.shared_device_name), devices.deviceName)
            this?.putString(getString(R.string.shared_device_address), devices.address)
            this?.putString(getString(R.string.shared_device_rssi), devices.rssi.toString())
            this?.commit()
        }
    }

    private fun updateRecyclerList() {
        with(recycler) {
            adapter =
                DevicesRecyclerViewAdapter(repo, clicked = {
                    val bluetoothDevice =
                        BluetoothAdapter.getDefaultAdapter().getRemoteDevice(it.address)
                    viewModel.connect(bluetoothDevice)
                    alertDialog = showConnectingDialog()
                }, menuDeleteClicked = {
                    repo.delete(it)
                    updateRecyclerList()
                })
        }
    }
//endregion

    //region lifecycle handlers
    override fun onResume() {
        super.onResume()
        viewModel.connectionStateLiveData?.observe(
            viewLifecycleOwner,
            Observer { handleConnectionState(it.state) })

        viewModel.bleManagerRecievedData?.observe(viewLifecycleOwner, Observer { checkCommand(it) })

        val recentDevice = SavedDevicesFragmentArgs.fromBundle(requireArguments()).recentDevice
        if (!viewModel.myBleManager?.isConnected!! && recentDevice != null) {
            val bluetoothDevice =
                BluetoothAdapter.getDefaultAdapter().getRemoteDevice(recentDevice.address)
            viewModel.connect(bluetoothDevice)
            alertDialog = showConnectingDialog()
        }
        updateRecyclerList()
    }

    override fun onDetach() {
        super.onDetach()
        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.sendData(constructSendCommand("Disconnect"))
        viewModel.disconnect()
    }
//endregion

}
