package com.symeonchen.wakeupscreen.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.symeonchen.wakeupscreen.data.NotifyItem


/**
 * Created by SymeonChen on 2020/5/2.
 */
@Dao
interface NotifyItemDao {
    @Query("SELECT * from notify_item_table ORDER BY id DESC")
    fun getAllNotifyItem(): LiveData<List<NotifyItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notifyItem: NotifyItem)

    @Query("DELETE FROM notify_item_table")
    suspend fun deleteAll()
}