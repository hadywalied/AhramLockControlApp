package com.github.hadywalied.ahramlockcontrolapp.ui

import android.os.Bundle
import androidx.navigation.findNavController
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.base.BaseActivity
import com.github.hadywalied.ahramlockcontrolapp.tag
import timber.log.Timber

class MainActivity : BaseActivity() {
    override fun onBackPressed() {
//        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Timber.plant(Timber.DebugTree())
        Timber.tag(tag)
        Timber.d("App Created")
    }
}