package com.github.hadywalied.ahramlockcontrolapp

import android.os.Bundle
import timber.log.Timber
import com.jakewharton.rxbinding4.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Timber.plant(Timber.DebugTree())
        Timber.tag(tag)
        Timber.d("App Created")

    }
}