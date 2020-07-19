package com.github.hadywalied.ahramlockcontrolapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.Records
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.RecordsRepo

@FunctionalInterface
interface OnDeviceClicked {
    fun clicked(@NonNull device: Devices)
}

/**
 * [RecyclerView.Adapter] that can display [Devices].
 *
 */
class DevicesRecyclerViewAdapter(
    private val repo: DevicesRepo,
    val listener: OnDeviceClicked
) : RecyclerView.Adapter<DevicesRecyclerViewAdapter.ViewHolder>() {

    private val values: List<Devices> = repo.getAll()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (values.isNotEmpty()) {
            val item = values[position]
            holder.bind(device = item)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(device: Devices) {

        }
    }
}


/**
 * [RecyclerView.Adapter] that can display [Records].
 *
 */
class RecordsRecyclerViewAdapter(
    repo: RecordsRepo
) : RecyclerView.Adapter<RecordsRecyclerViewAdapter.ViewHolder>() {
    private val values: List<Records> = repo.getAll()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (values.isNotEmpty()) {
            val item = values[position]
            holder.bind(record = item)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(record: Records) {

        }
    }
}

