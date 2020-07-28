package com.github.hadywalied.ahramlockcontrolapp.ui.controlpanel

import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.alexjlockwood.kyrie.*
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_control_panel.*
import kotlinx.android.synthetic.main.fragment_control_panel.toolbar
import kotlinx.android.synthetic.main.fragment_user_devices.*
import kotlinx.android.synthetic.main.locker_layout.*
import java.nio.file.Files.size
import java.util.concurrent.TimeUnit


class ControlPanelFragment : BaseFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        return inflater.inflate(R.layout.fragment_control_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val delay: Long = 6000
        val device = ControlPanelFragmentArgs.fromBundle(requireArguments()).connectedDevice
        toolbar.title = device.deviceName
        toolbar.subtitle = device.address
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            viewModel.disconnect()
            findNavController().navigateUp()
            findNavController().popBackStack()
        }
        val drawable = kyrieDrawable {
            viewport(48f, 48f)
            tint(Color.RED)
            group {
                translateX(24f)
                translateY(24f)
                rotation(
                    Animation.ofFloat(0f, 720f)
                        .duration(4444)
                        .repeatCount(Animation.INFINITE)
                )
                path {
                    strokeColor(Color.WHITE)
                    strokeWidth(4f)
                    trimPathStart(
                        Animation.ofFloat(0f, 0.75f)
                            .duration(1333)
                            .repeatCount(Animation.INFINITE)
                            .interpolator("M 0 0 h .5 C .7 0 .6 1 1 1".asPathInterpolator())
                    )
                    trimPathEnd(
                        Animation.ofFloat(0.03f, 0.78f)
                            .duration(1333)
                            .repeatCount(Animation.INFINITE)
                            .interpolator("M 0 0 c .2 0 .1 1 .5 1 C 1 1 1 1 1 1".asPathInterpolator())
                    )
                    trimPathOffset(
                        Animation.ofFloat(0f, 0.25f)
                            .duration(1333)
                            .repeatCount(Animation.INFINITE)
                    )
                    strokeLineCap(StrokeLineCap.SQUARE)
                    pathData("M 0 -18 a 18 18 0 1 1 0 36 18 18 0 1 1 0 -36")
                }
            }
        }
        btn_count.setImageDrawable(drawable)
        drawable.start()
        addDisposable(btn_count.clicks().throttleFirst(delay, TimeUnit.MILLISECONDS).subscribe {
//            btn_count.text = "unlocked"
            viewModel.sendData(constructSendCommand("UnLock", device.address))
            object : CountDownTimer(delay, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    tv_counter.text =
                        "seconds remaining till Locking: " + (millisUntilFinished / 1000).toString()
                }

                override fun onFinish() {
                    tv_counter.text = "Locked!"
//                    btn_count.text = "UnLock"
                    viewModel.sendData(constructSendCommand("Lock", device.address))
                }
            }.start()
        })

        addDisposable(fab_records.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_controlPanelFragment_to_recordsFragment)
        })
        addDisposable(chip_basic.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_controlPanelFragment_to_basicInfoFragment)
        })
        addDisposable(chip_admins.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            findNavController().navigate(R.id.action_controlPanelFragment_to_usersFragment)
        })

    }

}