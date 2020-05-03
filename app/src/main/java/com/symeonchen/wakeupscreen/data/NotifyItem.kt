package com.symeonchen.wakeupscreen.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Entity(tableName = "notify_item_table")
class NotifyItem {
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long = 0L

    @ColumnInfo(name = "time")
    var time: Long = 0L

    @ColumnInfo(name = "appName")
    var appName: String = ""
}