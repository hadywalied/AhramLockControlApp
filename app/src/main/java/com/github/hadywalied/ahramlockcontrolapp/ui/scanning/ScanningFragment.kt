package com.github.hadywalied.ahramlockcontrolapp.ui.scanning

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.recycler_layout.*
import kotlinx.android.synthetic.main.fragment_scanning.*
import kotlinx.android.synthetic.main.fragment_scanning.state_scanning
import kotlinx.android.synthetic.main.fragment_scanning.toolbar
import kotlinx.android.synthetic.main.fragment_user_devices.*
import kotlinx.android.synthetic.main.info_no_devices_found_layout.*
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import java.util.concurrent.TimeUnit

class ScanningFragment : BaseFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        //region observers
        viewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { onScanningFinished(it) })
        viewModel.allBluetoothDevicesLiveData.observe(
            viewLifecycleOwner,
            Observer { updateRecyclerList(it) })
        viewModel.scanFailedLiveData.observe(viewLifecycleOwner, Observer { scanFailedAction(it) })
        viewModel.connectFailedLiveData.observe(
            viewLifecycleOwner,
            Observer { connectionFailedAction(it) })
        viewModel.connectionStateLiveData?.observe(
            viewLifecycleOwner,
            Observer { connectedAction(it.state) })
        //endregion
        return inflater.inflate(R.layout.fragment_scanning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        viewModel.scan()
        //region toolbar
        toolbar.inflateMenu(R.menu.scan_menu)
        toolbar.subtitle = "Please Choose your Device"
        toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.menu_devices_item -> {
                    findNavController().navigate(R.id.action_scanningFragment_to_userDevicesFragment)
                    true
                }
                else -> false
            }
        }
        //endregion
        swipe?.setOnRefreshListener { viewModel.scan() }
        addDisposable(action_refresh.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            scanning_recycler_layout.visibility = View.VISIBLE
            scanning_no_devices_found_layout.visibility = View.GONE
            viewModel.scan()
        })
        state_scanning.isIndeterminate = true
        state_scanning.visibility = View.VISIBLE
    }

    //region helper functions
    private fun onScanningFinished(bool: Boolean) {
        swipe?.isRefreshing = bool
        if (bool)
            state_scanning.visibility = View.VISIBLE
        else state_scanning.visibility = View.INVISIBLE
        if (!bool && viewModel.devicesItems.isNullOrEmpty()) {
            scanning_recycler_layout.visibility = View.GONE
            scanning_no_devices_found_layout.visibility = View.VISIBLE
        } else {
            scanning_recycler_layout.visibility = View.VISIBLE
            scanning_no_devices_found_layout.visibility = View.GONE
        }
    }

    private fun connectionFailedAction(b: Boolean?) {
        showMaterialDialog(b!!)
    }

    private fun scanFailedAction(b: Boolean?) {
        showMaterialDialog(b!!)
    }

    private fun connectedAction(b: ConnectionState.State) {
        when (b) {
            ConnectionState.State.DISCONNECTED -> showMaterialDialog(false)
            else -> {
                viewModel.devicesItems.forEach {
                    if (it.address == viewModel.myBleManager?.bluetoothDevice?.address) {
                        repo.insert(it)
                        findNavController().navigate(R.id.action_scanningFragment_to_userDevicesFragment)
                        return@forEach
                    }
                }
            }
        }
    }

    private fun updateRecyclerList(list: List<Devices>?) {
        with(recycler) {
            adapter =
                DevicesRecyclerViewAdapter(repo, list, {
                    viewModel.connect(viewModel.devicesSet[it.address]!!)
                }, {
//                    repo.delete(it)
                })
        }
    }

    fun showMaterialDialog(bool: Boolean) {
        with(MaterialAlertDialogBuilder(requireContext())) {
            when (bool) {
                false -> {
                    setView(R.layout.info_failed_layout)
                    setTitle("Try Again")
                }
                true -> {
                    setView(R.layout.info_success_layout)
                    setTitle("Success")
                }
            }
            setNeutralButton("continue") { dialogInterface, _ -> dialogInterface.dismiss() }
            show()
        }
    }
//endregion
}
