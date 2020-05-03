package com.symeonchen.wakeupscreen.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.symeonchen.wakeupscreen.ScRoomDatabase
import com.symeonchen.wakeupscreen.data.NotifyItem
import com.symeonchen.wakeupscreen.repository.NotifyItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by SymeonChen on 2020/5/3.
 */
class NotifyItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotifyItemRepository
    val allNotifyItem: LiveData<List<NotifyItem>>

    init {
        val notifyItemDao = ScRoomDatabase.getDatabase(application).notifyItemDao()
        repository = NotifyItemRepository(notifyItemDao)
        allNotifyItem = repository.allNotifyItems
    }

    fun insert(notifyItem: NotifyItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(notifyItem)
    }

    fun clearAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

}