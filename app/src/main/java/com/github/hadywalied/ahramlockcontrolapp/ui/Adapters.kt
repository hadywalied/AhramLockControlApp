package com.github.hadywalied.ahramlockcontrolapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.Records
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.RecordsRepo
import kotlin.math.abs

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
        val deviceName = view.findViewById<TextView?>(R.id.device_item_name)
        val address = view.findViewById<TextView?>(R.id.device_item_mac)
        val rssi = view.findViewById<TextView?>(R.id.rssi_item_text)
        val rssiIcon = view.findViewById<ImageView?>(R.id.rssi_item_icon)

        fun bind(device: Devices) {
            device.also {
                deviceName.text = it.deviceName
                address.text = it.address
                rssi.text = it.rssi.toString()
                when (abs(it.rssi)) {
                    in 0..50 -> {
                        rssiIcon.setColorFilter(Color.GREEN)
                    }
                    in 50..70 -> {
                        rssiIcon.setColorFilter(Color.YELLOW)
                    }
                    else -> {
                        rssiIcon.setColorFilter(Color.RED)
                    }
                }
            }
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
            .inflate(R.layout.records_item, parent, false)
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

        val nameRecord = view.findViewById<TextView>(R.id.record_item_name)
        val addressRecord = view.findViewById<TextView>(R.id.record_item_addr)
        val dateTime = view.findViewById<TextView>(R.id.record_item_time)

        fun bind(record: Records) {
            nameRecord.text = record.name
            addressRecord.text = record.address
            dateTime.text = record.localDateTime
        }
    }
}

