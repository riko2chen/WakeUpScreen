package com.symeonchen.wakeupscreen.services.reminder

import com.symeonchen.wakeupscreen.services.notification.BlockReason
import org.junit.Assert.*
import org.junit.Test

class ReminderPolicyTest {
    @Test fun postsAndUpdatesCannotPostponeAnExistingDeadline() {
        var deadline = ReminderPolicy.nextDeadline(0, 100, 60)
        repeat(100) { deadline = ReminderPolicy.nextDeadline(deadline, 110L + it, 60) }
        assertEquals(160, deadline)
    }

    @Test fun limitedBatchRemainsExhaustedAndUnlimitedNeverExhausts() {
        assertTrue(ReminderPolicy.hasRoundsRemaining(2, 3))
        assertFalse(ReminderPolicy.hasRoundsRemaining(3, 3))
        assertFalse(ReminderPolicy.hasRoundsRemaining(5, 3))
        assertTrue(ReminderPolicy.hasRoundsRemaining(1_000_000, 0))
    }

    @Test fun silentNewestDoesNotSuppressEligibleOlderMessage() {
        val checks = mutableListOf<Int>()
        val selected = ReminderPolicy.evaluate(listOf(3, 2, 1), check = {
            checks += it
            if (it == 3) BlockReason.LOW_IMPORTANCE else null
        }, reason = { it })
        assertEquals(2, selected?.first)
        assertEquals(listOf(3, 2), checks)
        assertNull(selected?.second)
    }

    @Test fun globalGatesStopTheWholeBatchWithoutCheckingEveryMessage() {
        for (block in listOf(BlockReason.POCKET_MODE, BlockReason.FACE_DOWN, BlockReason.INTERACTIVE,
            BlockReason.SLEEP_MODE, BlockReason.DND, BlockReason.CHARGING, BlockReason.BATTERY_LEVEL)) {
            var checks = 0
            val selected = ReminderPolicy.evaluate(listOf(3, 2, 1), check = {
                checks++
                if (it == 3) BlockReason.LOW_IMPORTANCE else block
            }, reason = { it })
            assertEquals(block, selected?.second)
            assertEquals(2, checks)
        }
    }

    @Test fun noEligibleCandidateNeverProducesSuccess() {
        val selected = ReminderPolicy.evaluate(listOf(3, 2, 1), check = { BlockReason.LOW_IMPORTANCE }, reason = { it })
        assertEquals(BlockReason.LOW_IMPORTANCE, selected?.second)
        assertNull(ReminderPolicy.evaluate(emptyList<Int>(), check = { null as String? }, reason = { it }))
    }
}
