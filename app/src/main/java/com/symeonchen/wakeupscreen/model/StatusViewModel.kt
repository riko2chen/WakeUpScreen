package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by SymeonChen on 2019-10-27.
 */
class StatusViewModel : ViewModel() {

    var statusOfService: MutableLiveData<Boolean> = MutableLiveData()

    var permissionOfReadNotification: MutableLiveData<Boolean> = MutableLiveData()

}