package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatusViewModel : ViewModel() {

    var statusOfService: MutableLiveData<Boolean> = MutableLiveData()

    var permissionOfReadNotification: MutableLiveData<Boolean> = MutableLiveData()

}