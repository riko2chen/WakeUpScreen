package com.symeonchen.wakeupscreen.utils

import android.app.Application
import com.symeonchen.wakeupscreen.ScRoomDatabase
import com.symeonchen.wakeupscreen.data.NotifyItem
import com.symeonchen.wakeupscreen.repository.NotifyItemRepository

/**
 * Created by SymeonChen on 2019-10-27.
 */
class NotifyDataUtils {
    companion object {
        @Synchronized
        suspend fun addData(notifyItem: NotifyItem, application: Application) {
            val notifyItemDao = ScRoomDatabase.getDatabase(application).notifyItemDao()
            val repository = NotifyItemRepository(notifyItemDao)
            repository.insert(notifyItem)
        }
    }
}