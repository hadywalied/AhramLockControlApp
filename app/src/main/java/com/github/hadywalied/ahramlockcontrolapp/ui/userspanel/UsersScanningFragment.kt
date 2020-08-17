package com.github.hadywalied.ahramlockcontrolapp.ui.userspanel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_users_scanning.*
import kotlinx.android.synthetic.main.info_no_devices_found_layout.*
import kotlinx.android.synthetic.main.recycler_layout.*
import java.util.concurrent.TimeUnit


class UsersScanningFragment : BaseFragment() {

    lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        //region observers
        viewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { onScanningFinished(it) })
        viewModel.allBluetoothDevicesLiveData.observe(
            viewLifecycleOwner,
            Observer { updateRecyclerList(it) })
        viewModel.bleManagerRecievedData?.observe(viewLifecycleOwner, Observer { checkcommand(it) })
        //endregion

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        return inflater.inflate(R.layout.fragment_users_scanning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        viewModel.scanUsers()
        //region toolbar
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.subtitle = "Please Choose The User's Device"
        //endregion
        swipe.setOnRefreshListener {
            viewModel.scanUsers()
            Handler(Looper.getMainLooper()).postDelayed({
                swipe.isRefreshing = false
            }, 1200)
        }
        addDisposable(action_refresh.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            scanning_recycler_layout.visibility = View.VISIBLE
            scanning_no_devices_found_layout.visibility = View.GONE
            viewModel.scanUsers()
        })
        state_scanning.isIndeterminate = true
        state_scanning.visibility = View.VISIBLE
    }

    private fun onScanningFinished(bool: Boolean) {
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

    private fun updateRecyclerList(list: List<Devices>?) {
        with(recycler) {
            adapter =
                DevicesRecyclerViewAdapter(repo, list ?: listOf(), clicked = {
                    val arguments = it.address.filter { it.isLetterOrDigit() }
                    viewModel.sendData(constructSendCommand("AddUser", arguments))
                },
                    menuDeleteClicked = {})
        }
    }

    private fun checkcommand(s: String?) {
        val split: List<String> = s?.split("|")!!
        when (split[0]) {
            "AUM" -> {
                showDialog(3)
            }
            "AUME" -> {
                showDialog(4)
            }
        }
    }

    private fun showDialog(i: Int): AlertDialog {
        return with(MaterialAlertDialogBuilder(requireActivity())) {
            setTitle("Adding User")
            setCancelable(false)
            when (i) {
                1 -> {
                    setMessage("User is being added")
                    setNeutralButton("cancel") { dialogInterface, i ->
                        viewModel.sendData(constructSendCommand("CancelAddingUser"))
                        dialogInterface.dismiss()
                    }
                }
                2 -> {
                    setMessage("Adding User Failed")
                    setNeutralButton("dismiss") { dialogInterface, i ->
                        viewModel.sendData(constructSendCommand("CancelAddingUser"))
                        dialogInterface.dismiss()
                    }
                }
                3 -> {
                    setView(R.layout.info_success_layout)
                    setNeutralButton("Dismiss") { dialogInterface, i ->
                        dialogInterface.dismiss()
                        findNavController().popBackStack()
                    }
                }
                else -> {
                    setMessage("Cancelled")
                    setNeutralButton("Dismiss") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                }
            }
            create()
            show()
        }

    }


}