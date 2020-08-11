package com.github.hadywalied.ahramlockcontrolapp.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseFragment
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.domain.RecordsRepo
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.github.hadywalied.ahramlockcontrolapp.ui.RecordsRecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.android.synthetic.main.fragment_records.*
import kotlinx.android.synthetic.main.fragment_user_devices.toolbar
import java.util.concurrent.TimeUnit

class RecordsFragment : BaseFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: RecordsRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bleManagerConnectionState?.observe(
            viewLifecycleOwner,
            Observer { if (!it) showDisconnectedDialog() })

        repo = Injector.ProvideRecordsRepo(requireContext())
        toolbar.navigationIcon =
            ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            viewModel.disconnect()
            findNavController().navigateUp()
        }
        addDisposable(
            fab_refresh_records.clicks().throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
                updateRecyclerList()
                recycler_records.adapter?.notifyDataSetChanged()
            })
        updateRecyclerList()

    }

    private fun showDisconnectedDialog() {
        //TODO ADD MAterial Dialog For Disconnection
    }

    private fun updateRecyclerList() {
        with(recycler_records) {
            adapter =
                RecordsRecyclerViewAdapter(repo)
        }
    }

}