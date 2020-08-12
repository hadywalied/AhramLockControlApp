package com.github.hadywalied.ahramlockcontrolapp.ui.userspanel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R


class UsersScanningFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_users_scanning, container, false)
    }

}