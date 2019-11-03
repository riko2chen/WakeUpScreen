package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.NotifyItem
import io.realm.Realm

/**
 * Created by SymeonChen on 2019-10-27.
 */
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