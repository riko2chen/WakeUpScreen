package com.symeonchen.wakeupscreen

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.symeonchen.wakeupscreen.dao.NotifyItemDao
import com.symeonchen.wakeupscreen.data.NotifyItem


/**
 * Created by SymeonChen on 2020/5/2.
 */
@Database(entities = [NotifyItem::class], version = 1, exportSchema = true)
abstract class ScRoomDatabase : RoomDatabase() {

    abstract fun notifyItemDao(): NotifyItemDao

    companion object {
        @Volatile
        private var INSTANCE: ScRoomDatabase? = null

        fun getDatabase(context: Context): ScRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScRoomDatabase::class.java,
                    "wakeupscreen_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}