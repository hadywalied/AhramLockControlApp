package com.github.hadywalied.ahramlockcontrolapp.ui.userspanel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.Users
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.github.hadywalied.ahramlockcontrolapp.ui.UsersRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_scanning.*
import kotlinx.android.synthetic.main.recycler_layout.*

class UsersFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.users_menu)
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
        updateRecyclerList()
    }

    private fun initiateAddUserSequence() {
//        TODO initiateAddUserSequence
    }

    private fun initiateNFCSequence() {
//        TODO initiateNFCSequence
    }

    private fun updateRecyclerList() {
        with(recycler) {
            adapter =
                UsersRecyclerViewAdapter() {}
        }
    }


}