package com.symeonchen.wakeupscreen

import android.app.Application
import com.tencent.mmkv.MMKV
import io.realm.Realm


@Suppress("unused")
class ScApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        Realm.init(this)
    }

}
