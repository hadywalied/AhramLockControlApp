package com.github.hadywalied.ahramlockcontrolapp.domain

import android.content.Context
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.RecordsRepo
import com.github.hadywalied.ahramlockcontrolapp.domain.RoomDB

object Injector {
    fun ProvideDevicesRepo(context: Context): DevicesRepo {
        val database = RoomDB.getInstance(context)
        return DevicesRepo(database.devicesDAO())
    }

    fun ProvideRecordsRepo(context: Context): RecordsRepo {
        val database = RoomDB.getInstance(context)
        return RecordsRepo(database.recordsDao())
    }
}