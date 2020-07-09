package com.github.hadywalied.ahramlockcontrolapp.ui.scanning

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.hadywalied.ahramlockcontrolapp.R

class ScanningFragment : Fragment() {

    companion object {
        fun newInstance() = ScanningFragment()
    }

    private lateinit var viewModel: ScanningViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.scanning_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ScanningViewModel::class.java)
        // TODO: Use the ViewModel
    }

}