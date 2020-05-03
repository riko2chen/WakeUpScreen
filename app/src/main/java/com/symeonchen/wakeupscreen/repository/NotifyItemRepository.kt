package com.symeonchen.wakeupscreen.repository

import androidx.lifecycle.LiveData
import com.symeonchen.wakeupscreen.dao.NotifyItemDao
import com.symeonchen.wakeupscreen.data.NotifyItem


/**
 * Created by SymeonChen on 2020/5/3.
 */
class NotifyItemRepository(private val notifyItemDao: NotifyItemDao) {
    val allNotifyItems: LiveData<List<NotifyItem>> = notifyItemDao.getAllNotifyItem()

    suspend fun insert(notifyItem: NotifyItem) {
        notifyItemDao.insert(notifyItem)
    }

    suspend fun deleteAll() {
        notifyItemDao.deleteAll()
    }
}