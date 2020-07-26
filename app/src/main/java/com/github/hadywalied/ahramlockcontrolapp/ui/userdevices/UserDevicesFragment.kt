package com.github.hadywalied.ahramlockcontrolapp.ui.userdevices

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import kotlinx.android.synthetic.main.fragment_user_devices.*
import kotlinx.android.synthetic.main.fragment_user_devices.all_devices_recycler
import kotlinx.android.synthetic.main.fragment_user_devices.toolbar

class UserDevicesFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        return inflater.inflate(R.layout.fragment_user_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_refresh)
        toolbar.setNavigationOnClickListener {
            viewModel.disconnect()
            findNavController().navigateUp()
        }
        updateRecyclerList()
        swipe.setOnRefreshListener {
            updateRecyclerList()
            Handler(Looper.getMainLooper()).postDelayed({ swipe.isRefreshing = false }, 1500)
        }
    }

    private fun updateRecyclerList() {
        with(all_devices_recycler) {
            adapter =
                DevicesRecyclerViewAdapter(repo, clicked = {
                    if (!viewModel.myBleManager?.isConnected!!) {
                        val bluetoothDevice =
                            BluetoothAdapter.getDefaultAdapter().getRemoteDevice(it.address)
                        viewModel.connect(bluetoothDevice)
                    }
                    UserDevicesFragmentDirections.actionUserDevicesFragmentToControlPanelFragment(it)
                        .let { action ->
                            findNavController().navigate(action.actionId, action.arguments)
                        }
                }, menuDeleteClicked = {
                    repo.delete(it)
                })
        }
    }


}