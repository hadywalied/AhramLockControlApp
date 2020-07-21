package com.github.hadywalied.ahramlockcontrolapp.ui.controlpanel

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.R
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_control_panel.*
import kotlinx.android.synthetic.main.locker_layout.*
import java.util.concurrent.TimeUnit


class ControlPanelFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val delay: Long = 6000
        addDisposable(btn_count.clicks().throttleFirst(delay, TimeUnit.MILLISECONDS).subscribe {
            btn_count.text = "unlocked"
            object : CountDownTimer(delay, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    tv_counter.text =
                        "seconds remaining till Locking: " + (millisUntilFinished / 1000).toString()
                }

                override fun onFinish() {
                    tv_counter.text = "Locked!"
                    btn_count.text = "UnLock"
                }
            }.start()
        })

        addDisposable(fab_records.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_controlPanelFragment_to_recordsFragment)
        })
        addDisposable(chip_basic.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_controlPanelFragment_to_basicInfoFragment)
        })

    }

}