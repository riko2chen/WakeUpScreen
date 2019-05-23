package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.NotifyItem
import io.realm.Realm

class NotifyDataUtils {
    companion object {
        @Synchronized
        fun addData(notifyItem: NotifyItem) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                it.copyToRealmOrUpdate(notifyItem)
            }
            realm.close()
        }
    }
}