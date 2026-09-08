package com.symeonchen.wakeupscreen.services.bluetooth

/** No cached connection may authorize a wake without current permission and pairing. */
object BluetoothWakePolicy {
    fun allows(enabled: Boolean, available: Boolean, permitted: Boolean, powered: Boolean,
               selected: Set<String>, paired: Set<String>, connected: Set<String>): Boolean =
        !enabled || (available && permitted && powered &&
            selected.any { it in paired && it in connected })
}
