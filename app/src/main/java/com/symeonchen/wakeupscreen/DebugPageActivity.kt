package com.symeonchen.wakeupscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.symeonchen.wakeupscreen.data.NotifyItem
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_debug_page.*
import java.text.SimpleDateFormat
import java.util.*

class DebugPageActivity : ScBaseActivity() {

    private var realm: Realm? = null
    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_page)
        realm = Realm.getDefaultInstance()
        initLogView()
    }

    companion object {
        fun actionStart(context: Context) {
            context.startActivity(Intent(context, DebugPageActivity::class.java))
        }
    }

    private fun initLogView() {
        val realmCallback: RealmChangeListener<RealmResults<NotifyItem>> = RealmChangeListener {
            if (it.size > 0) {
                var str = resources.getString(R.string.debug_page_hints)
                for (item in it) {
                    val itemStr = "\n ${sdf.format(item.time)} : ${item.appName}"
                    str += itemStr
                }
                tv_log.text = str
            }
        }

        val result = realm?.where(NotifyItem::class.java)
            ?.findAllAsync()
        result?.addChangeListener(realmCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
    }
}
