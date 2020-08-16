package com.github.hadywalied.ahramlockcontrolapp.base

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.UserType
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

open class BaseFragment : Fragment() {
    private lateinit var disposables: CompositeDisposable
    lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()

        model = ViewModelProvider(this@BaseFragment)[MainViewModel::class.java]
        /*
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })*/
    }

    override fun onResume() {
        super.onResume()
        model.bleManagerRecievedData?.observe(viewLifecycleOwner, Observer { checkCommand(it) })
    }

    private fun checkCommand(s: String?) {
        val split: List<String> = s?.split("|")!!
        when (split.get(0)) {
            "TO" -> {
                with(MaterialAlertDialogBuilder(requireContext())) {
                    setTitle("Time Out")
                    setMessage("The Connection timed out please reconnect again!")
                    setPositiveButton("Reconnect") { _, _ ->
                        requireActivity().recreate()
                    }
                    show()
                }
            }

        }
    }

    private fun init() {
        initRx()
    }

    private fun initRx() {
        disposables = CompositeDisposable()
    }

    @Synchronized
    protected fun addDisposable(disposable: Disposable?) {
        if (disposable == null) return
        disposables.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposables.isDisposed) disposables.dispose()
    }
}