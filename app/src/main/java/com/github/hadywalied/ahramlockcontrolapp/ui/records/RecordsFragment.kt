package com.github.hadywalied.ahramlockcontrolapp.ui.records

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.Records
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.domain.RecordsRepo
import com.github.hadywalied.ahramlockcontrolapp.getTimeDate
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.github.hadywalied.ahramlockcontrolapp.ui.RecordsRecyclerViewAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_records.*
import kotlinx.android.synthetic.main.fragment_user_devices.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import java.util.*
import java.util.concurrent.TimeUnit

class RecordsFragment : BaseFragment() {

    //region variables
    private lateinit var viewModel: MainViewModel
    private lateinit var repo: RecordsRepo
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

        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.connectionStateLiveData?.observe(
            viewLifecycleOwner,
            Observer { showDisconnectedDialog(it.state) })
        viewModel.bleManagerRecievedData?.observe(viewLifecycleOwner, Observer {
            checkCommand(it)
        })

        repo = Injector.ProvideRecordsRepo(requireContext())
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        addDisposable(
            fab_refresh_records.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                updateRecyclerList()
            })
        CoroutineScope(Dispatchers.IO).launch {
            if (repo.getAll().isEmpty()) {
                viewModel.sendData(constructSendCommand("GetAllRecords"))
            } else {
                viewModel.sendData(constructSendCommand("UpdateRecords"))
            }
        }
        updateRecyclerList()

    }

    //region helper functions
    private fun checkCommand(s: String?) {
        val split: List<String> = s?.split("|")!!
        when (split[0]) {
            "R" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    repo.recordsDao.insertRecord(
                        Records(
                            "${Random().nextInt()}", //TODO get Users Addresses
                            "User${split[1]}",
                            getTimeDate(split[2])
                        )
                    )
                }
                updateRecyclerList()
            }

        }
    }

    private fun showDisconnectedDialog(it: ConnectionState.State) {
        val dialog =
            MaterialAlertDialogBuilder(requireContext()).setView(R.layout.info_connection_lost)
                .setCancelable(false)
                .setNeutralButton("Reconnect") { dialogInterface, _ ->
                    run {
                        viewModel.sendData(constructSendCommand("CancelRecords"))
                        findNavController().popBackStack()
                        dialogInterface.dismiss()
                    }
                }.create()
        if (ConnectionState.State.CONNECTING == it || ConnectionState.State.READY == it) dialog.dismiss()
        else if (ConnectionState.State.DISCONNECTED == it) dialog.show()
    }

    private fun updateRecyclerList() {
        with(recycler_records) {
            adapter =
                RecordsRecyclerViewAdapter(repo)
        }
    }
//endregion

    override fun onPause() {
        super.onPause()
        viewModel.sendData(constructSendCommand("CancelRecords"))
    }

}