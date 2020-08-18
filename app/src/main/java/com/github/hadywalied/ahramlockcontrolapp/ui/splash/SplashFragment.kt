package com.github.hadywalied.ahramlockcontrolapp.ui.splash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.android.synthetic.main.fragment_splash.imageView
import kotlinx.coroutines.*

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            val flag = checkForFirstTimeUse()
            if (flag) {
                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController().navigate(
                        R.id.action_splashFragment_to_setupFragment,
                        null,
                        null,
                        FragmentNavigatorExtras(imageView to "logo Transition")
                    )
                }, 2000)
            } else
                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController().navigate(
                        R.id.action_splashFragment_to_welcomeScreenFragment,
                        null,
                        null,
                        FragmentNavigatorExtras(imageView to "logo Transition")
                    )
                }, 2000)
        }
    }

    //region helper functions
    private suspend fun checkForFirstTimeUse(): Boolean {
        val repo = Injector.ProvideDevicesRepo(requireContext())
        return repo.getAll().isEmpty()
    }
    //endregion

}