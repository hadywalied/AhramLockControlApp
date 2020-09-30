package com.github.hadywalied.ahramlockcontrolapp.ui.userspanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_phone_user.*
import java.util.concurrent.TimeUnit


class PhoneUserFragment : BaseFragment() {

    //region variables
    lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        //region observers
        viewModel.bleManagerRecievedData?.observe(viewLifecycleOwner, Observer { checkCommand(it) })
        //endregion

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        return inflater.inflate(R.layout.fragment_phone_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        //region toolbar
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.subtitle = "Please Choose The User's Device"
        //endregion

        addDisposable(
            btn_add_user.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                if (edit_add_phone?.text.isNullOrEmpty()) {
                    Toast.makeText(
                        requireActivity(),
                        "Please enter a phone number first",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (edit_add_phone.length() < 11)
                    Toast.makeText(
                        requireActivity(),
                        "Please enter Valid a phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    viewModel.sendData(
                        constructSendCommand(
                            "AddUser",
                            edit_add_phone.text.toString()
                        )
                    )
                }
            })

    }

    //region helper functions
    private fun checkCommand(s: String?) {
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
    //endregion

}