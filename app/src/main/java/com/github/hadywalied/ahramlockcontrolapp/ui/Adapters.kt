package com.github.hadywalied.ahramlockcontrolapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R

@FunctionalInterface
interface OnDeviceClicked {
    fun clicked(device: Devices)
}

/**
 * [RecyclerView.Adapter] that can display [Devices].
 *
 */
class MyDevicesRecyclerViewAdapter(private val values: List<Devices>, val listener: OnDeviceClicked) : RecyclerView.Adapter<MyDevicesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (values.isNotEmpty()) {
            val item = values[position]

        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }
}
