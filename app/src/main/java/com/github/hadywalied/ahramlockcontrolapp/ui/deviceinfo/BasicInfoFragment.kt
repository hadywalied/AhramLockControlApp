package com.github.hadywalied.ahramlockcontrolapp.ui.deviceinfo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.constructSendCommand
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.jakewharton.rxbinding4.widget.SeekBarChangeEvent
import com.jakewharton.rxbinding4.widget.changeEvents
import kotlinx.android.synthetic.main.fragment_basic_info.*
import kotlinx.android.synthetic.main.fragment_basic_info.toolbar
import kotlinx.android.synthetic.main.fragment_control_panel.*

class BasicInfoFragment : BaseFragment() {

    private lateinit var viewModel: MainViewModel

    private val shareprefs by lazy {
        activity?.getSharedPreferences(getString(R.string.sharedprefsfile), Context.MODE_PRIVATE)
    }
    var delay: Int? = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.bleManagerRecievedData?.observe(
            viewLifecycleOwner,
            Observer { handleBasicInfoResponses(it) })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        return inflater.inflate(R.layout.fragment_basic_info, container, false)
    }

    private fun handleBasicInfoResponses(s: String?) {
        val split: List<String> = s?.split("|")!!
        when (split.get(0)) {
            "B" -> {
                tv_battery.text = split[1]
            }
            "LT" -> {
                if (split[1] == "OK")
                    Toast.makeText(requireContext(), "Update ${split[1]}", Toast.LENGTH_SHORT)
                        .show()
                else delay = when (split[1]) {
                    "3" -> 0
                    "5" -> 1
                    "8" -> 2
                    else -> 0
                }
                seekbar_delay.progress = delay ?: 0
                text_delay.text = when (delay) {
                    0 -> "Lock Delay: 3 seconds"
                    1 -> "Lock Delay: 5 seconds"
                    2 -> "Lock Delay: 8 seconds"
                    else -> "Lock Delay: 3 seconds"
                }
                shareprefs?.edit()?.putInt(getString(R.string.delay_index), delay ?: 0)?.apply()
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        delay = shareprefs?.getInt(getString(R.string.delay_index), 0)
        seekbar_delay.progress = delay ?: 0
        text_delay.text = when (delay) {
            0 -> "Lock Delay: 3 seconds"
            1 -> "Lock Delay: 5 seconds"
            2 -> "Lock Delay: 8 seconds"
            else -> "Lock Delay: 3 seconds"
        }
        seekbar_delay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                delay = p0?.progress
                when (delay) {
                    0 -> {
                        text_delay.text = "Lock Delay: 3 seconds"
                        viewModel.sendData(constructSendCommand("SetUnlockDelay", "3"))
                    }
                    1 -> {
                        text_delay.text = "Lock Delay: 5 seconds"
                        viewModel.sendData(constructSendCommand("SetUnlockDelay", "5"))
                    }
                    2 -> {
                        text_delay.text = "Lock Delay: 8 seconds"
                        viewModel.sendData(constructSendCommand("SetUnlockDelay", "8"))
                    }
                }
                shareprefs?.edit()?.putInt(getString(R.string.delay_index), delay ?: 0)?.apply()
            }
        })
        viewModel.sendData(constructSendCommand("GetBattery"))
        viewModel.sendData(constructSendCommand("GetDelay"))
    }

}

