package com.github.hadywalied.ahramlockcontrolapp.domain

import android.content.Context
import androidx.room.*
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.Records
import kotlinx.coroutines.*

@Dao
interface RecordsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Records)

    @Query("select * from records")
    suspend fun getRecords(): List<Records>

    @Query("DELETE FROM records")
    suspend fun deleteAllRecords()
}

@Dao
interface DevicesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Devices)

    @Query("select * from devices")
    suspend fun getDevices(): List<Devices>

    @Delete
    suspend fun deleteDevice(device: Devices)
}

class RecordsRepo(val recordsDao: RecordsDAO) {
    companion object {
        @Volatile
        private var instance: RecordsRepo? = null
        fun getInstance(context: Context): RecordsRepo? {
            return instance ?: synchronized(RecordsRepo::class.java) {
                if (instance == null) {
                    val database = RoomDB.getInstance(context)
                    instance = RecordsRepo(database.recordsDao())
                }
                return instance
            }
        }
    }

    fun insert(record: Records) {
        CoroutineScope(Dispatchers.IO).launch {
            recordsDao.insertRecord(record)
        }
    }

    fun getAll() = runBlocking {
        var list = mutableListOf<Records>()
        CoroutineScope(Dispatchers.IO).launch {
            list = recordsDao.getRecords() as MutableList<Records>
        }
        return@runBlocking list
    }

    fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            recordsDao.deleteAllRecords()
        }
    }
}

class DevicesRepo(val devicesDAO: DevicesDAO) {
    companion object {
        @Volatile
        private var instance: DevicesRepo? = null
        fun getInstance(context: Context): DevicesRepo? {
            return instance ?: synchronized(DevicesRepo::class.java) {
                if (instance == null) {
                    val database = RoomDB.getInstance(context)
                    instance = DevicesRepo(database.devicesDAO())
                }
                return instance
            }
        }
    }

    fun insert(device: Devices) {
        CoroutineScope(Dispatchers.IO).launch {
            devicesDAO.insertDevice(device)
        }
    }

    fun delete(device: Devices) {
        CoroutineScope(Dispatchers.IO).launch {
            devicesDAO.deleteDevice(device)
        }
    }

    fun getAll(): List<Devices> = runBlocking {
        var list = mutableListOf<Devices>()
        CoroutineScope(Dispatchers.IO).launch {
            list = devicesDAO.getDevices() as MutableList<Devices>
        }
        return@runBlocking list
    }
}

@Database(
    entities = [Records::class, Devices::class],
    version = 1,
    exportSchema = false
)
abstract class RoomDB : RoomDatabase() {
    abstract fun recordsDao(): RecordsDAO
    abstract fun devicesDAO(): DevicesDAO

    companion object {
        @Volatile
        private var INSTANCE: RoomDB? = null

        fun getInstance(context: Context): RoomDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                RoomDB::class.java, "AhramApp.db"
            )
                .build()
    }
}