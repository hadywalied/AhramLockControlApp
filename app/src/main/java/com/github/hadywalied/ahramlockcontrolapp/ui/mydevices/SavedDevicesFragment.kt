package com.github.hadywalied.ahramlockcontrolapp.ui.mydevices

import android.bluetooth.BluetoothAdapter
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
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
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
            Observer { handleConnectionState(it.state) })
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_user_devices, container, false)
    }

    var alertDialog: AlertDialog? = null
    private fun handleConnectionState(state: ConnectionState.State) {
        when (state) {
            ConnectionState.State.INITIALIZING -> {
                alertDialog?.dismiss()
                SavedDevicesFragmentDirections.actionUserDevicesFragmentToControlPanelFragment(
                    Devices(
                        viewModel.myBleManager?.bluetoothDevice!!.address,
                        viewModel.myBleManager?.bluetoothDevice!!.name,
                        0
                    )
                )
                    .let { action ->
                        findNavController().navigate(action.actionId, action.arguments)
                    }
            }
            ConnectionState.State.CONNECTING -> {
                val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleLarge)
                progressBar.isIndeterminate = true
                alertDialog = MaterialAlertDialogBuilder(requireContext()).setTitle("Connecting")
                    .setBackground(resources.getDrawable(R.color.primaryTextColor,resources.newTheme()))
                    .setNegativeButton("Cancel") { dialogInterface, i ->
                        dialogInterface.dismiss()
                        viewModel.disconnect()
                    }
                    .setCancelable(false)
                    .setView(progressBar).create()
                alertDialog!!.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    if(ConnectionState.State.DISCONNECTED == state){
                        Toast.makeText(
                            requireContext(),
                            "Connection Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog!!.dismiss()
                    }
                }, 1500)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            viewModel.disconnect()
            findNavController().navigateUp()
        }
        updateRecyclerList()
        swipe.setOnRefreshListener {
            updateRecyclerList()
            Handler(Looper.getMainLooper()).postDelayed({ swipe?.isRefreshing = false }, 1500)
        }
    }

    private fun updateRecyclerList() {
        with(recycler) {
            adapter =
                DevicesRecyclerViewAdapter(repo, clicked = {
                    if (!viewModel.myBleManager?.isConnected!!) {
                        val bluetoothDevice =
                            BluetoothAdapter.getDefaultAdapter().getRemoteDevice(it.address)
                        viewModel.connect(bluetoothDevice)
                    }

                }, menuDeleteClicked = {
                    repo.delete(it)
                    updateRecyclerList()
                })
        }
    }


}