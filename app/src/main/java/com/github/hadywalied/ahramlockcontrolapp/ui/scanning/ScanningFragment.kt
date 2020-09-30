package com.github.hadywalied.ahramlockcontrolapp.ui.scanning

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.*
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_scanning.*
import kotlinx.android.synthetic.main.info_no_devices_found_layout.*
import kotlinx.android.synthetic.main.recycler_layout.*
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ScanningFragment : BaseFragment() {

    //region variables
    private lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo
    private var qrEader: QREader? = null
    private var alertDialog: AlertDialog? = null
    var permissionGranted = false
    private val shareprefs by lazy {
        activity?.getSharedPreferences(getString(R.string.sharedprefsfile), Context.MODE_PRIVATE)
    }
//endregion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
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
            Observer {
                connectedAction(it.state)
            })
        viewModel.bleManagerRecievedData?.observe(viewLifecycleOwner, Observer { checkCommand(it) })
        //endregion
        return inflater.inflate(R.layout.fragment_scanning, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        viewModel.scan()
        //region toolbar
        toolbar.inflateMenu(R.menu.scan_menu)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.subtitle = "Please Choose your Device"
        toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.menu_devices_item -> {
                    ScanningFragmentDirections.actionScanningFragmentToUserDevicesFragment(null)
                        .let { action ->
                            findNavController().navigate(action.actionId, action.arguments)
                        }
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

    private fun checkCommand(s: String?) {
        val split: List<String> = s?.split("|")!!
        when (split[0]) {
            "C" -> {
                when (split[1]) {
                    "0" -> {
                        viewModel.sendData(constructSendCommand("Sync", getCurrentTimeDate()))
                        navigateWhenConnected(UserType.ADMIN)
                    }
                    "1" -> navigateWhenConnected(UserType.USER)
                    "F" -> {
                        alertDialog?.dismiss()
                        Toast.makeText(requireContext(), "Connection Failed", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            }
            "S" -> {
                alertDialog?.cancel()
                qrEader?.stop()
                if (split[1] == "T") {
//                    viewModel.sendData(constructSendCommand("Sync", getCurrentTimeDate()))
                    navigateWhenConnected(UserType.ADMIN)
                } else {
                    Toast.makeText(requireContext(), "Connection Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else -> {
                Timber.d(s)
            }
        }
    }

    private fun navigateWhenConnected(userType: UserType) {
        alertDialog?.dismiss()
        viewModel.devicesItems.forEach {
            if (it.address == viewModel.myBleManager?.bluetoothDevice?.address) {
                repo.insert(it)
                ScanningFragmentDirections.actionScanningFragmentToControlPanelFragment(
                    it,
                    userType
                ).let { action ->
                    findNavController().navigate(action.actionId, action.arguments)
                }
                return@forEach
            }
        }
    }

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
            ConnectionState.State.READY -> {
                val devices = Devices(
                    viewModel.myBleManager?.bluetoothDevice?.address ?: "",
                    viewModel.myBleManager?.bluetoothDevice?.name ?: "",
                    0
                )
                if (devices.address.isNotEmpty()) showConnectionPrompt(devices)
            }
            ConnectionState.State.INITIALIZING -> {
                alertDialog?.dismiss()
            }
            ConnectionState.State.DISCONNECTED -> {
                alertDialog?.dismiss()
            }
        }
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


    private fun updateRecyclerList(list: List<Devices>?) {
        with(recycler) {
            adapter =
                DevicesRecyclerViewAdapter(repo, list, {
                    viewModel.sendData(constructSendCommand("Disconnect"))
                    viewModel.disconnect()
                    connectToDevice(it)
                    alertDialog = showConnectingDialog()
                }, {})
        }
    }

    private fun connectToDevice(devices: Devices) {
        viewModel.devicesSet[devices.address]?.let {
            viewModel.connect(it)
        }
    }

    private fun showConnectionPrompt(device: Devices) {
        with(MaterialAlertDialogBuilder(requireContext())) {
            setTitle("Connection Prompt")
            setMessage("Please Choose Connection Type")
            setCancelable(false)
            setPositiveButton("Scan Qr Code") { dialogInterface, _ ->
                dialogInterface.dismiss()
                alertDialog = qrScannerDialog()
                alertDialog?.show()
                if (permissionGranted) {
                    val cameraView = alertDialog?.findViewById<SurfaceView>(R.id.camera_view)
                    qrEader =
                        QREader.Builder(requireContext(), cameraView, QRDataListener { value ->
                            Timber.d("Recieved Pin: $value")
                            Observable.empty<String>()
                                .defaultIfEmpty(value)
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe { s: String ->
                                    sendAdminCommand(
                                        BluetoothAdapter.getDefaultAdapter().address,
                                        s
                                    )
                                    alertDialog?.dismiss()
                                    alertDialog = showConnectingDialog()
                                    qrEader?.stop()
                                }
                        }).facing(QREader.BACK_CAM)
                            .enableAutofocus(true)
                            .width(cameraView?.width!!)
                            .height(cameraView.height)
                            .build()
                    qrEader?.initAndStart(cameraView)
                }

            }
            setNegativeButton("Connect") { dialogInterface, _ ->
                sendUserCommand(BluetoothAdapter.getDefaultAdapter().address)
                dialogInterface.dismiss()
                alertDialog = showConnectingDialog()
            }
            setNeutralButton("cancel") { dialogInterface, i ->
                dialogInterface.dismiss()
                alertDialog?.dismiss()
                viewModel.sendData(constructSendCommand("Disconnect"))
                viewModel.disconnect()
            }
            show()
        }
    }

    private fun qrScannerDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext()).setTitle("Scan Qr Code")
            .setMessage("Please scan the QR-Code Provided on the Lock Package")
            .setCancelable(true)
            .setView(R.layout.scanner_layout)
            .create()
    }

    private fun sendUserCommand(device: String) {
        val arguments =
            shareprefs?.getString(getString(R.string.phone_number), "")
        viewModel.sendData(
            constructSendCommand(
                "Connect",
                arguments ?: ""
            )
        )
    }

    @UiThread
    private fun sendAdminCommand(device: String, pin: String?) {
        val arguments =
            shareprefs?.getString(getString(R.string.phone_number), "") ?: ""
        viewModel.sendData(constructSendCommand("Setup", arguments, pin ?: "0"))
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

    //region life cycle handlers
    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    override fun onPause() {
        super.onPause()
        qrEader?.stop()
        qrEader?.releaseAndCleanup()
    }

    override fun onDetach() {
        super.onDetach()
        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.sendData(constructSendCommand("Disconnect"))
        viewModel.disconnect()
    }
//endregion
}
