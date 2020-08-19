package com.github.hadywalied.ahramlockcontrolapp.ui.setupscreen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.ui.MainActivity
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.github.hadywalied.ahramlockcontrolapp.ui.setupscreen.slider.*
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_setup.*
import java.util.concurrent.TimeUnit

class SetupFragment : BaseFragment() {

    private val fragmentList = arrayListOf<Fragment>()

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

        val adapter = SliderAdapter(requireActivity())
        vpIntroSlider.adapter = adapter

        fragmentList.clear()
        fragmentList.addAll(
            listOf(
                FragmentOne(), FragmentTwo(), FragmentThree(), FragmentFour(), FragmentFive()
            )
        )
        adapter.setFragmentList(fragmentList)

        indicatorLayout.setIndicatorCount(adapter.itemCount)
        indicatorLayout.selectCurrentPosition(0)

        vpIntroSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                indicatorLayout.selectCurrentPosition(position)

                if (position < fragmentList.lastIndex) {
                    tvSkip.visibility = View.VISIBLE
                    tvNext.text = "Next"
                } else {
                    tvSkip.visibility = View.GONE
                    tvNext.text = "Get Started"
                }
            }
        })


        tvSkip.setOnClickListener {
            findNavController().navigate(R.id.action_setupFragment_to_scanningFragment)
        }

        tvNext.setOnClickListener {
            val position = vpIntroSlider.currentItem

            if (position < fragmentList.lastIndex) {
                vpIntroSlider.currentItem = position + 1
            } else {
                findNavController().navigate(R.id.action_setupFragment_to_scanningFragment)
            }
        }
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