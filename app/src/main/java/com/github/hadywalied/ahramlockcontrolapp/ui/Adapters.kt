package com.github.hadywalied.ahramlockcontrolapp.ui

import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.Records
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.RecordsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * [RecyclerView.Adapter] that can display [Devices].
 *
 */
class DevicesRecyclerViewAdapter(
    private val repo: DevicesRepo, private val list: List<Devices>? = listOf(),
    private val clicked: (Devices) -> Unit, private val menuDeleteClicked: (Devices) -> Unit
) : RecyclerView.Adapter<DevicesRecyclerViewAdapter.ViewHolder>() {

    private var values: List<Devices> = list ?: listOf()

    val job = CoroutineScope(Dispatchers.IO).launch {
        if (list.isNullOrEmpty()) values = repo.getAll()
    }

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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        val deviceName = view.findViewById<TextView>(R.id.device_item_name)
        val address = view.findViewById<TextView>(R.id.device_item_mac)
        val rssi = view.findViewById<TextView>(R.id.rssi_item_text)
        val rssiIcon = view.findViewById<ImageView>(R.id.rssi_item_icon)
        val layout = view.findViewById<LinearLayout>(R.id.devices_item_layout)
        var selDevice: Devices? = null
        fun bind(device: Devices) {
            selDevice = device
            device.also {
                deviceName.text = it.deviceName ?: "Unnamed Device"
                address.text = it.address
                rssi.text = it.rssi.toString()
                //TODO Change The RSSI Text
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
                layout.setOnClickListener { _ -> clicked(it) }
                if (list.isNullOrEmpty()) layout.setOnCreateContextMenuListener(this)
            }
        }

        override fun onCreateContextMenu(
            p0: ContextMenu?,
            p1: View?,
            p2: ContextMenu.ContextMenuInfo?
        ) {
            p0?.add(0, p1?.id!!, 0, "Remove")?.setOnMenuItemClickListener {
                selDevice?.let { it1 -> menuDeleteClicked(it1) }
                return@setOnMenuItemClickListener true
            }//groupId, itemId, order, title
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
    private var values: List<Records> = listOf()
    val job = CoroutineScope(Dispatchers.IO).launch { values = repo.getAll() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.records_item, parent, false)
        job.start()
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

