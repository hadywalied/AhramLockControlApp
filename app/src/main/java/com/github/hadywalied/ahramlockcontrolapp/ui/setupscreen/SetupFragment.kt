package com.github.hadywalied.ahramlockcontrolapp.ui.setupscreen

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_setup.*
import java.util.concurrent.TimeUnit

class SetupFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addDisposable(
            layout_continue.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                findNavController().navigate(R.id.action_setupFragment_to_scanningFragment)
            })
    }

    override fun onDetach() {
        super.onDetach()
        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        if (viewModel.myBleManager?.isConnected!!) {
            viewModel.sendData(constructSendCommand("Disconnect"))
            viewModel.disconnect()
        }
    }

}