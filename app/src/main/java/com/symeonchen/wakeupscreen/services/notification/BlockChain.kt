package com.symeonchen.wakeupscreen.services.notification

import android.app.Application
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * What happened at one node of the chain.
 */
enum class ChainNodeState {
    /** The notification got past this gate. */
    PASSED,

    /** This gate is the one that stopped it. */
    BLOCKED,

    /** Switched off, so it never took part in the decision. */
    SKIPPED,

    /**
     * Armed, but whether it blocks depends on the notification itself. Only
     * ever produced by [BlockChain.liveSnapshot], where there is no
     * notification to judge.
     */
    DEPENDS,

    /** Something earlier already blocked; this gate was never reached. */
    NOT_EVALUATED,

    /** Terminal node: the screen was woken. */
    REACHED,
}

data class ChainStep(val key: String, val state: ChainNodeState)

/**
 * Turns the condition chain into something a screen can draw.
 *
 * The order of the gates is not repeated here — it comes from
 * [ListenerManager.orderedKeys], so registering a new condition adds a node to
 * both views automatically instead of letting the diagram drift away from the
 * logic it claims to describe.
 */
object BlockChain {

    /** Leading node of the live view: without this nothing arrives at all. */
    const val KEY_NOTIFICATION_ACCESS = "notification_access"

    /** Terminal node: the screen lights up. */
    const val KEY_WAKE_UP = "wake_up"

    /**
     * Every gate in order, from the service's own pre-check through the
     * registered conditions. Excludes [KEY_NOTIFICATION_ACCESS], which is a
     * system permission rather than a gate a delivered notification passed.
     */
    fun gateKeys(): List<String> = buildList {
        add(BlockReason.APP_SWITCH_OFF)
        addAll(ListenerManager.orderedKeys())
    }

    /**
     * The gates [liveSnapshot] walks.
     *
     * [BlockReason.INTERACTIVE] is deliberately left out. It is a real gate,
     * and the notification log shows it as such — but on a page describing
     * what the settings would do, it can only ever report the state of the
     * screen the reader is looking at. Asking it produces "blocking" every
     * time and truncates everything below; not asking it produces a node
     * permanently stuck on "undecided". Neither tells the reader anything, so
     * the row is not drawn at all.
     */
    fun liveGateKeys(): List<String> = buildList {
        add(KEY_NOTIFICATION_ACCESS)
        add(BlockReason.APP_SWITCH_OFF)
        addAll(ListenerManager.orderedKeys().filter(::isLiveGate))
    }

    private fun isLiveGate(key: String): Boolean = key != BlockReason.INTERACTIVE

    /**
     * Replays a recorded outcome.
     *
     * Nothing is evaluated here: the entry already says which gate stopped it,
     * and the order says the rest got past. That makes this exact regardless
     * of how the settings have changed since.
     *
     * Returns an empty list when the entry is not a chain outcome — a
     * [LogStatus.REMINDER_STOPPED] row, or a reason from a build that knew
     * gates this one does not — so callers can fall back to the plain text
     * description rather than draw a misleading diagram.
     */
    fun forLogEntry(entry: NotificationLogEntry): List<ChainStep> {
        val blockingKey: String? = when (entry.status) {
            LogStatus.WAKED_UP -> null
            // Recorded with its own status rather than BLOCKED, but it is the
            // interactive gate that stopped it all the same.
            LogStatus.SCREEN_ALREADY_ON -> entry.blockReason.ifEmpty { BlockReason.INTERACTIVE }
            LogStatus.BLOCKED -> entry.blockReason
            LogStatus.REMINDER_STOPPED -> return emptyList()
        }

        val gates = gateKeys()
        if (blockingKey != null && blockingKey !in gates) {
            return emptyList()
        }

        val steps = mutableListOf<ChainStep>()
        var blocked = false
        for (key in gates) {
            val state = when {
                blocked -> ChainNodeState.NOT_EVALUATED
                key == blockingKey -> {
                    blocked = true
                    ChainNodeState.BLOCKED
                }
                else -> ChainNodeState.PASSED
            }
            steps.add(ChainStep(key, state))
        }
        steps.add(
            ChainStep(
                KEY_WAKE_UP,
                if (blocked) ChainNodeState.NOT_EVALUATED else ChainNodeState.REACHED,
            )
        )
        return steps
    }

    /**
     * Describes what a notification arriving right now would run into, from
     * the current settings and device state.
     *
     * Gates that cannot answer without a notification report
     * [ChainNodeState.DEPENDS] and let the walk continue, so the reader still
     * sees the whole chain rather than a view truncated by an unknown.
     */
    fun liveSnapshot(
        application: Application?,
        hasNotificationAccess: Boolean,
    ): List<ChainStep> {
        val steps = mutableListOf<ChainStep>()
        var blocked = false

        fun walk(key: String, armed: Boolean, blocksNow: Boolean?) {
            val state = when {
                blocked -> ChainNodeState.NOT_EVALUATED
                !armed -> ChainNodeState.SKIPPED
                blocksNow == null -> ChainNodeState.DEPENDS
                blocksNow -> {
                    blocked = true
                    ChainNodeState.BLOCKED
                }
                else -> ChainNodeState.PASSED
            }
            steps.add(ChainStep(key, state))
        }

        walk(KEY_NOTIFICATION_ACCESS, armed = true, blocksNow = !hasNotificationAccess)
        walk(BlockReason.APP_SWITCH_OFF, armed = true, blocksNow = !DataInjection.switchOfApp)
        for (condition in ListenerManager.conditions().filter { isLiveGate(it.key) }) {
            walk(condition.key, condition.isArmed(), condition.wouldBlockNow(application))
        }
        steps.add(
            ChainStep(
                KEY_WAKE_UP,
                if (blocked) ChainNodeState.NOT_EVALUATED else ChainNodeState.REACHED,
            )
        )
        return steps
    }
}
