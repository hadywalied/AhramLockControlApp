package com.github.hadywalied.ahramlockcontrolapp.ui.welcome

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.animationsandcustomviews.circularRevealAnimation
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.concurrent.TimeUnit

class WelcomeScreenFragment : BaseFragment() {

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
    }

    private fun showLoggedInActions() {
        Handler(
            Looper.getMainLooper()
        ).postDelayed({ card_logged_in.circularRevealAnimation() }, 500)
        addClickHandles()
    }

    fun addClickHandles() {
        addDisposable(btn_add_device.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_loginFragment_to_scanningFragment)
        })
        addDisposable(
            btn_all_devices.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                findNavController().navigate(R.id.action_loginFragment_to_userDevicesFragment)
            })
    }
}