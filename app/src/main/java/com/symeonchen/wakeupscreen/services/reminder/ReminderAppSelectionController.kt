package com.symeonchen.wakeupscreen.services.reminder

import android.content.Context
import com.symeonchen.wakeupscreen.data.ReminderAppSelection
import com.symeonchen.wakeupscreen.utils.DataInjection

object ReminderAppSelectionController {
    /** Called after saving or restoring app eligibility; evaluate existing notifications immediately. */
    fun onChanged(context: Context) {
        val app = context.applicationContext
        val selection = ReminderAppSelection.load()
        if (!DataInjection.switchOfApp || !DataInjection.repeatReminderSwitch ||
            (selection.custom && selection.packages.isEmpty())) {
            ReminderScheduler.cancel(app)
            return
        }
        // Reconcile eligibility without restarting an existing batch or treating
        // a temporarily unavailable listener as an empty notification shade.
        ReminderEngine.onSettingsChanged(app)
    }
}
