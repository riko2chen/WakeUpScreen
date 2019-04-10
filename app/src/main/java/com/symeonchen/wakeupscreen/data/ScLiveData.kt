package com.symeonchen.wakeupscreen.data

import androidx.lifecycle.LiveData

class ScLiveData<T> : LiveData<T>() {

    var listener: OnLiveDataValueInput<T>? = null

    public override fun setValue(value: T) {
        super.setValue(value)
        listener?.onValueInput(value)
    }


    public override fun postValue(value: T) {
        super.postValue(value)
        listener?.onValueInput(value)
    }

    interface OnLiveDataValueInput<T> {
        fun onValueInput(value: T)
    }
}