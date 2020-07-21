package com.github.hadywalied.ahramlockcontrolapp.ui.login

import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.R
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.concurrent.TimeUnit

class LoginFragment : BaseFragment() {

    val shareprefs by lazy {
        activity?.getSharedPreferences(getString(R.string.sharedprefsfile), Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        val username = shareprefs?.getString(getString(R.string.username), "")
        if (username?.isNotEmpty()!! || username.isNotBlank())
            findNavController().navigate(R.id.action_loginFragment_to_scanningFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addDisposable(btn_continue.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            //TODO Save the Name and continue
            if (et_username.text.isNotEmpty() || et_username.text.isNotBlank()) {
                with(shareprefs?.edit()) {
                    this?.putString(getString(R.string.username), et_username.text.toString())
                    this?.commit()
                }
                findNavController().navigate(R.id.action_loginFragment_to_scanningFragment)
            }
        })
    }

}