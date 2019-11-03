package com.symeonchen.wakeupscreen.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.NotifyItem
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_debug_page.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
class DebugPageActivity : ScBaseActivity() {

    private var realm: Realm? = null
    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var results: RealmResults<NotifyItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_page)
        realm = Realm.getDefaultInstance()
        setListener()
        initLogView()
    }

    companion object {
        fun actionStart(context: Context) {
            context.startActivity(Intent(context, DebugPageActivity::class.java))
        }
    }

    private fun initLogView() {

        results = realm?.where(NotifyItem::class.java)
            ?.findAll()?.sort("time", Sort.DESCENDING)
        updateView(results)
    }

    private fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_clear_all.setOnClickListener {
            results?.let {
                realm?.executeTransaction {
                    results?.deleteAllFromRealm()
                }
                tv_log.text = ""
            }
        }
    }

    private fun updateView(it: RealmResults<NotifyItem>?) {
        it ?: return
        if (it.size > 0) {
            var str = ""
            for (item in it) {
                val itemStr = "\n ${sdf.format(item.time)} : ${item.appName}"
                str += itemStr
            }
            tv_log.text = str
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
    }
}
