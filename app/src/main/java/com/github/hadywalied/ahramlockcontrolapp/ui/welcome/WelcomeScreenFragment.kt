package com.github.hadywalied.ahramlockcontrolapp.ui.welcome

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.animationsandcustomviews.circularRevealAnimation
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_device.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WelcomeScreenFragment : BaseFragment() {

    private val shareprefs by lazy {
        activity?.getSharedPreferences(getString(R.string.sharedprefsfile), Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoggedInActions()
        CoroutineScope(Dispatchers.IO).launch { addRecentDeviceLayout() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            with(MaterialAlertDialogBuilder(requireContext())) {
                this?.setTitle("Exit")
                this?.setPositiveButton("yes") { dialogInterface, i ->
                    requireActivity().finish()

                }
                this?.setNegativeButton("cancel") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                create()
                show()
            }
        }

    }

    private suspend fun addRecentDeviceLayout() {
        if (Injector.ProvideDevicesRepo(requireContext()).getAll().isEmpty()
            || shareprefs?.getString(getString(R.string.shared_device_name), "").isNullOrEmpty()
        ) {
            layout_recently_used.visibility = View.GONE
        } else {
            val itemName = shareprefs?.getString(getString(R.string.shared_device_name), "")
            val itemAddress = shareprefs?.getString(getString(R.string.shared_device_address), "")
            val itemRssi = shareprefs?.getString(getString(R.string.shared_device_rssi), "")
            addDisposable(
                layout_recently_used.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val devices = Devices(itemAddress ?: "", itemName, itemRssi?.toInt() ?: 0)
                        WelcomeScreenFragmentDirections.actionWelcomeScreenFragmentToUserDevicesFragment(
                            devices
                        )
                            .let { action ->
                                findNavController().navigate(action.actionId, action.arguments)
                            }
                    })
            device_item_name.text = itemName
            device_item_mac.text = itemAddress
//            rssi_item_text.text = itemRssi
        }
    }

    private fun showLoggedInActions() {
        Handler(
            Looper.getMainLooper()
        ).postDelayed({ card_logged_in.circularRevealAnimation() }, 250)
        addClickHandles()
    }

    private fun addClickHandles() {
        addDisposable(btn_add_device.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_welcomeScreenFragment_to_scanningFragment)
        })
        addDisposable(
            btn_all_devices.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                WelcomeScreenFragmentDirections.actionWelcomeScreenFragmentToUserDevicesFragment(
                    null
                )
                    .let { action ->
                        findNavController().navigate(action.actionId, action.arguments)
                    }
            })
    }

    override fun onDetach() {
        super.onDetach()
        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.disconnect()
    }

}