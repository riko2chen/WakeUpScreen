package com.symeonchen.wakeupscreen.services.reminder

import android.content.Context
import com.symeonchen.wakeupscreen.data.ReminderAppSelection
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.UnreadNotificationUtils

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
        val active = try { ScNotificationListenerService.instance?.activeNotifications }
            catch (_: Exception) { null }
        // A missing listener snapshot is not evidence that notifications were dismissed.
        // Reconnection and the next alarm will evaluate the persisted selection again.
        if (active == null) return
        if (UnreadNotificationUtils.hasUnread(active)) {
            ReminderScheduler.restartStreak(app)
        } else {
            ReminderScheduler.cancel(app)
        }
    }
}
