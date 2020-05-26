package com.symeonchen.wakeupscreen.model

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.symeonchen.wakeupscreen.data.AppInfo
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.pages.FilterListActivity
import com.symeonchen.wakeupscreen.states.AppListState
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.FilterListUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


/**
 * Created by SymeonChen on 2020/5/25.
 */
class FilterListViewModel(application: Application) : AndroidViewModel(application) {
    private var viewModelApplication: Application = application

    private var appList: MutableList<AppInfo>? = null
    private var map: HashMap<String, Int>? = null

    var currentModeValue = CurrentMode.MODE_WHITE_LIST.ordinal
    var visibleList: MutableLiveData<MutableList<AppInfo>> =
        MutableLiveData(mutableListOf())

    var includeSystemApp: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            calculateFilter()
        }
    }
    var searchKey: String by Delegates.observable("") { _, oldValue, newValue ->
        if (newValue != oldValue) {
            calculateFilter()
        }
    }


    fun initIntent(intent: Intent) {
        currentModeValue =
            intent.getIntExtra(FilterListActivity.CURRENT_MODE, CurrentMode.MODE_WHITE_LIST.ordinal)
        map = if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            FilterListUtils.getMapFromString(DataInjection.appBlackListStringOfNotify)
        } else {
            FilterListUtils.getMapFromString(DataInjection.appWhiteListStringOfNotify)
        }
    }

    fun readAllApp() {
        viewModelScope.launch(Dispatchers.IO) {
            appList =
                AppListState.getInstalledAppList(viewModelApplication.applicationContext, true)
            calculateFilter()
        }
    }

    fun saveList() {
        val result = HashMap<String, Int>()
        for (item in appList ?: mutableListOf()) {
            if (item.selected) {
                result[item.packageName] = 1
            }
        }
        if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            DataInjection.appBlackListStringOfNotify = FilterListUtils.saveMapToString(result)
        } else {
            DataInjection.appWhiteListStringOfNotify = FilterListUtils.saveMapToString(result)
        }
        DataInjection.appListUpdateFlag = System.currentTimeMillis()
    }

    private fun calculateFilter() {
        val result = appList
            ?.filter {
                if (!includeSystemApp) {
                    !it.systemApp
                } else {
                    true
                }
            }
            ?.filter {
                if (searchKey.isEmpty()) {
                    true
                } else {
                    it.simpleName.contains(searchKey, true)
                }
            }
            ?: mutableListOf()
        visibleList.postValue(FilterListUtils.splitWithSelected(result, map))
    }

}