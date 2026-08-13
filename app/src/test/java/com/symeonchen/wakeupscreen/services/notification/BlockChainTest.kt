package com.symeonchen.wakeupscreen.services.notification

import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The chain view claims to describe what the listener actually does. These
 * tests are what keeps that claim true as conditions are added or reordered.
 */
class BlockChainTest {

    private fun entry(
        status: LogStatus,
        reason: String = "",
    ) = NotificationLogEntry(
        timestamp = 0L,
        packageName = "com.example.app",
        status = status,
        blockReason = reason,
    )

    private fun states(steps: List<ChainStep>) = steps.associate { it.key to it.state }

    @Test
    fun `gate order matches the registered conditions`() {
        // Reordering the chain changes which gate gets the credit for a block,
        // so it should be a deliberate act that trips this test.
        assertEquals(
            listOf(
                BlockReason.APP_SWITCH_OFF,
                BlockReason.POCKET_MODE,
                BlockReason.INTERACTIVE,
                BlockReason.FILTER_LIST,
                BlockReason.LOW_IMPORTANCE,
                BlockReason.ONGOING,
                BlockReason.SLEEP_MODE,
                BlockReason.DND,
                BlockReason.CHARGING,
                BlockReason.BATTERY_LEVEL,
            ),
            BlockChain.gateKeys(),
        )
    }

    @Test
    fun `the live view drops the screen check and keeps everything else`() {
        // It can only ever report the state of the screen being read from, so
        // it says nothing about the configuration the page describes. The log
        // still shows it, because there it is a recorded fact.
        val live = BlockChain.liveGateKeys()
        assertTrue(BlockReason.INTERACTIVE !in live)
        assertTrue(BlockReason.INTERACTIVE in BlockChain.gateKeys())
        assertEquals(
            listOf(BlockChain.KEY_NOTIFICATION_ACCESS) +
                BlockChain.gateKeys().filter { it != BlockReason.INTERACTIVE },
            live,
        )
    }

    @Test
    fun `every gate has a node and every node is reachable`() {
        val steps = BlockChain.forLogEntry(entry(LogStatus.WAKED_UP))
        assertEquals(BlockChain.gateKeys() + BlockChain.KEY_WAKE_UP, steps.map { it.key })
    }

    @Test
    fun `a woken notification passes every gate`() {
        val steps = BlockChain.forLogEntry(entry(LogStatus.WAKED_UP))
        val byKey = states(steps)
        BlockChain.gateKeys().forEach {
            assertEquals("gate $it", ChainNodeState.PASSED, byKey[it])
        }
        assertEquals(ChainNodeState.REACHED, byKey[BlockChain.KEY_WAKE_UP])
    }

    @Test
    fun `the blocking gate splits the chain in three`() {
        val steps = BlockChain.forLogEntry(
            entry(LogStatus.BLOCKED, BlockReason.SLEEP_MODE)
        )
        val byKey = states(steps)

        // Everything before sleep mode got past.
        assertEquals(ChainNodeState.PASSED, byKey[BlockReason.APP_SWITCH_OFF])
        assertEquals(ChainNodeState.PASSED, byKey[BlockReason.POCKET_MODE])
        assertEquals(ChainNodeState.PASSED, byKey[BlockReason.FILTER_LIST])

        assertEquals(ChainNodeState.BLOCKED, byKey[BlockReason.SLEEP_MODE])

        // Everything after it never ran.
        assertEquals(ChainNodeState.NOT_EVALUATED, byKey[BlockReason.DND])
        assertEquals(ChainNodeState.NOT_EVALUATED, byKey[BlockReason.CHARGING])
        assertEquals(ChainNodeState.NOT_EVALUATED, byKey[BlockChain.KEY_WAKE_UP])
    }

    @Test
    fun `exactly one node is ever blocked`() {
        BlockChain.gateKeys().forEach { reason ->
            val steps = BlockChain.forLogEntry(entry(LogStatus.BLOCKED, reason))
            assertEquals(
                "reason $reason",
                1,
                steps.count { it.state == ChainNodeState.BLOCKED },
            )
        }
    }

    @Test
    fun `the first gate blocking leaves nothing passed`() {
        val steps = BlockChain.forLogEntry(
            entry(LogStatus.BLOCKED, BlockReason.APP_SWITCH_OFF)
        )
        assertTrue(steps.none { it.state == ChainNodeState.PASSED })
        assertEquals(ChainNodeState.BLOCKED, steps.first().state)
    }

    @Test
    fun `screen already on is attributed to the interactive gate`() {
        // Recorded under its own status rather than BLOCKED, but it is still
        // InteractiveCondition that stopped it.
        val steps = BlockChain.forLogEntry(
            entry(LogStatus.SCREEN_ALREADY_ON, BlockReason.INTERACTIVE)
        )
        assertEquals(ChainNodeState.BLOCKED, states(steps)[BlockReason.INTERACTIVE])
    }

    @Test
    fun `screen already on without a recorded reason still resolves`() {
        val steps = BlockChain.forLogEntry(entry(LogStatus.SCREEN_ALREADY_ON))
        assertEquals(ChainNodeState.BLOCKED, states(steps)[BlockReason.INTERACTIVE])
    }

    @Test
    fun `reminder streak endings are not chain outcomes`() {
        // These carry a reminder vocabulary rather than a gate name; drawing a
        // chain for them would invent a decision that never happened.
        listOf(
            BlockReason.REMINDER_ALL_READ,
            BlockReason.REMINDER_MAX_ROUNDS,
            BlockReason.REMINDER_SWITCH_OFF,
            BlockReason.REMINDER_SERVICE_UNAVAILABLE,
        ).forEach { reason ->
            assertTrue(
                "reason $reason",
                BlockChain.forLogEntry(
                    entry(LogStatus.REMINDER_STOPPED, reason)
                ).isEmpty(),
            )
        }
    }

    @Test
    fun `an unknown block reason yields no diagram`() {
        // Rather than draw every gate as passed and quietly lose the blocker.
        assertTrue(
            BlockChain.forLogEntry(
                entry(LogStatus.BLOCKED, "reason_from_a_newer_build")
            ).isEmpty()
        )
    }
}
