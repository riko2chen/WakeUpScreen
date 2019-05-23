package com.symeonchen.wakeupscreen.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class NotifyItem : RealmObject() {
    @PrimaryKey
    var id: Long = 0L
    var time: Long = 0L
    var appName: String = ""
}