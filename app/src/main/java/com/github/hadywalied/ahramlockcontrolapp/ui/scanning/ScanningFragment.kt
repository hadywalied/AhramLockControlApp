package com.github.hadywalied.ahramlockcontrolapp.ui.scanning

import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import github.nisrulz.qreader.QRDataListener
import github.nisrulz.qreader.QREader
import kotlinx.android.synthetic.main.recycler_layout.*
import kotlinx.android.synthetic.main.fragment_scanning.*
import kotlinx.android.synthetic.main.fragment_scanning.state_scanning
import kotlinx.android.synthetic.main.fragment_scanning.toolbar
import kotlinx.android.synthetic.main.info_no_devices_found_layout.*
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import java.util.concurrent.TimeUnit

class ScanningFragment : BaseFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo
    private var qrEader: QREader? = null
    private var alertDialog: AlertDialog? = null
    var permissionGranted = false

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
        /* viewModel.scanFailedLiveData.observe(viewLifecycleOwner, Observer { scanFailedAction(it) })
         viewModel.connectFailedLiveData.observe(
             viewLifecycleOwner,
             Observer { connectionFailedAction(it) })*/
        viewModel.connectionStateLiveData?.observe(
            viewLifecycleOwner,
            Observer { connectedAction(it.state) })
        //endregion

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
            findNavController().popBackStack()
        }

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
        //region alertDialog
        alertDialog = AlertDialog.Builder(requireContext()).setTitle("Scan Qr Code")
            .setMessage("Please scan the QR-Code Provided on the Lock Package")
            .setCancelable(true)
            .setView(R.layout.scanner_layout)
            .create()
        //endregion
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

    private fun connectedAction(b: ConnectionState.State) {
        when (b) {
            ConnectionState.State.DISCONNECTING -> showMaterialDialog(false)
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
                    showConnectionPrompt(it)
                }, {})
        }
    }

    private fun showConnectionPrompt(device: Devices) {
        with(MaterialAlertDialogBuilder(requireContext())) {
            setTitle("Connection Prompt")
            setMessage("Please Choose Connection Type")
            setPositiveButton("Scan Qr Code") { dialogInterface, _ ->
                run {
                    dialogInterface.dismiss()
                    alertDialog?.show()
                    if (permissionGranted) {
                        alertDialog?.show()

                        val cameraView = alertDialog?.findViewById<SurfaceView>(R.id.camera_view)
                        qrEader = QREader.Builder(requireContext(), cameraView, QRDataListener {
                            sendAdminCommand(device, it)
                            alertDialog?.dismiss()
                        }).facing(QREader.BACK_CAM)
                            .enableAutofocus(true)
                            .width(cameraView?.width!!)
                            .height(cameraView.height)
                            .build()
                        qrEader?.initAndStart(cameraView)
                        qrEader?.start()
                    }
                }
            }
            setNeutralButton("Connect") { dialogInterface, _ ->
                sendUserCommand(device)
                dialogInterface.dismiss()
            }
            show()
        }
    }

    private fun sendUserCommand(device: Devices) {
        viewModel.devicesSet[device.address]?.let {
            viewModel.connect(it)
        }
        TODO("Not yet implemented")
    }

    private fun sendAdminCommand(device: Devices, pin: String?) {
        TODO("Not yet implemented")
    }

    private fun showMaterialDialog(bool: Boolean) {
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

    private fun requestPermissions() {
        Dexter.withContext(requireActivity())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
//                        setupQReader()
//                        setupCamera()
                    permissionGranted = true
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Please Provide Camera Permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    permissionGranted = false
                }
            })
            .check()
    }

//endregion

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    override fun onPause() {
        super.onPause()
        qrEader?.releaseAndCleanup()
    }

}
