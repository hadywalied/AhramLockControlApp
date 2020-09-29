package com.github.hadywalied.ahramlockcontrolapp.ui.userspanel

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.Users
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.github.hadywalied.ahramlockcontrolapp.ui.UsersRecyclerViewAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.swiperefreshlayout.refreshes
import kotlinx.android.synthetic.main.fragment_scanning.*
import kotlinx.android.synthetic.main.fragment_scanning.toolbar
import kotlinx.android.synthetic.main.fragment_user_devices.*
import kotlinx.android.synthetic.main.recycler_layout.*

class UsersFragment : BaseFragment() {

    //region variables
    lateinit var viewModel: MainViewModel
    val list = mutableListOf<Users>()
    var alertDialog: AlertDialog? = null
//endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.bleManagerRecievedData?.observe(viewLifecycleOwner, Observer { checkcommand(it) })
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            viewModel.sendData(constructSendCommand("CancelGetUsers"))
            viewModel.sendData(constructSendCommand("CancelAddingUser"))
            viewModel.sendData(constructSendCommand("CancelAddingNFC"))
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe.isRefreshing = false
        viewModel.sendData(constructSendCommand("GetUsers"))
        toolbar.inflateMenu(R.menu.users_menu)
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
//            viewModel.sendData(constructSendCommand("CancelGetUsers"))
        }
        toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.nfc_menu_item -> {
                    initiateNFCSequence()
                    true
                }
                R.id.add_user_menu_item -> {
                    initiateAddUserSequence()
                    true
                }
                else -> false
            }
        }
        addDisposable(swipe.refreshes().subscribe {
            list.clear()
            viewModel.sendData(constructSendCommand("GetUsers"))
            Handler(Looper.getMainLooper()).postDelayed({
                swipe?.isRefreshing = false
//                viewModel.sendData(constructSendCommand("CancelGetUsers"))
            }, 5000)
        })
        updateRecyclerList()
    }

    //region helper functions
    private fun checkcommand(s: String?) {
        val split: List<String> = s?.split("|")!!
        when (split[0]) {
            "W" -> {
                if (split.size > 2) {
                    list.add(Users(split[1], "User ${split[1]}", split[2]))
                    updateRecyclerList()
                }
            }
            "WE" -> {
                Toast.makeText(requireActivity(), "Users Updated", Toast.LENGTH_SHORT).show()
            }
            "AUC" -> when (split[1]) {
                "AE" -> {
                    alertDialog?.cancel()
                    alertDialog = showDialog(1)
                }
                "UC" -> {
                    alertDialog?.cancel()
                    alertDialog = showDialog(2)
                }
                else -> {
                    alertDialog?.cancel()
                    alertDialog = showDialog(3)
                    list.add(Users(split[1], "Card ${split[1]}", ""))
                    updateRecyclerList()
                }
            }
            "CAUC" -> {
                Toast.makeText(requireActivity(), "Operation Canceled", Toast.LENGTH_SHORT).show()

            }
            "DU" -> {
                Toast.makeText(requireActivity(), "User Removed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDialog(i: Int): AlertDialog {
        return with(MaterialAlertDialogBuilder(requireActivity())) {
            setTitle("Adding a NFC Card")
            when (i) {
                1 -> {
                    setMessage("The Card Already Exists")
                    setNeutralButton("Dismiss") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                }
                2 -> {
                    setMessage("Please Pass the NFC Tag")
                    setNeutralButton("cancel") { dialogInterface, i ->
                        viewModel.sendData(constructSendCommand("CancelAddingNFC"))
                        dialogInterface.dismiss()
                    }
                }
                else -> {
                    setView(R.layout.info_success_layout)
                    setNeutralButton("Dismiss") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                }
            }
            create()
            show()
        }

    }


    private fun initiateAddUserSequence() {
        findNavController().navigate(R.id.action_usersFragment_to_usersScanningFragment)
    }

    private fun initiateNFCSequence() {
        viewModel.sendData(constructSendCommand("AddNFC"))
    }

    private fun updateRecyclerList() {
        with(recycler) {
            adapter =
                UsersRecyclerViewAdapter(list, menuDeleteClicked = {
                    viewModel.sendData(constructSendCommand("RmUser", it.id))
                    list.remove(it)
                    adapter?.notifyDataSetChanged()
                })
        }
    }
//endregion

}