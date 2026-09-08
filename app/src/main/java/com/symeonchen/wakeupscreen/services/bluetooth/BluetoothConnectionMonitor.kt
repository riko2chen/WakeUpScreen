package com.symeonchen.wakeupscreen.services.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.symeonchen.wakeupscreen.utils.DataInjection

/** Public audio profile and GATT snapshots seed already-connected devices after process death.
 * ACL broadcasts cover subsequent connections, including profiles without public proxy APIs.
 * Bonding alone NEVER counts as connection. Unknown connections fail closed.
 */
@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
object BluetoothConnectionMonitor {
    data class Device(val address: String, val name: String)
    data class Snapshot(val available: Boolean, val permitted: Boolean, val powered: Boolean,
                        val devices: List<Device> = emptyList(), val connected: Set<String> = emptySet(),
                        val initializing: Boolean = false) {
        fun allows(enabled: Boolean, selected: Set<String>) = BluetoothWakePolicy.allows(
            enabled, available, permitted, powered, selected, devices.map { it.address }.toSet(), connected)
        fun needsStartupWait(enabled: Boolean, selected: Set<String>): Boolean =
            enabled && initializing && available && permitted && powered &&
                !allows(enabled, selected) && devices.any { it.address in selected }
    }
    private var context: Context? = null
    private var adapter: BluetoothAdapter? = null
    private val owners = mutableSetOf<Any>()
    private val proxies = mutableMapOf<Int, BluetoothProfile>()
    private val acl = mutableSetOf<String>()
    private val unavailableProfiles = mutableSetOf<Int>()
    private val discovery = BluetoothStartupDiscovery(SystemClock::elapsedRealtime)
    private var generation = 0
    private var registered = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            synchronized(this@BluetoothConnectionMonitor) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                try {
                    when (intent.action) {
                        BluetoothDevice.ACTION_ACL_CONNECTED -> device?.let { acl.add(it.address) }
                        BluetoothDevice.ACTION_ACL_DISCONNECTED -> device?.let { acl.remove(it.address) }
                        BluetoothDevice.ACTION_BOND_STATE_CHANGED -> if (intent.getIntExtra(
                            BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE) == BluetoothDevice.BOND_NONE
                        ) device?.let { acl.remove(it.address) }
                        BluetoothAdapter.ACTION_STATE_CHANGED -> {
                            stopMonitoring()
                            ensureMonitoring(context)
                        }
                    }
                } catch (_: SecurityException) { stopMonitoring() }
            }
        }
    }

    fun hasPermission(context: Context): Boolean = Build.VERSION.SDK_INT < 31 ||
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    @Synchronized fun acquire(context: Context, owner: Any) {
        owners.add(owner)
        ensureMonitoring(context.applicationContext)
    }
    @Synchronized fun release(owner: Any) {
        owners.remove(owner)
        if (owners.isEmpty()) stopMonitoring()
    }
    @Synchronized fun settingsChanged(context: Context) {
        if (!DataInjection.bluetoothWakeSwitch) stopMonitoring() else ensureMonitoring(context.applicationContext)
    }
    private fun ensureMonitoring(ctx: Context) {
        if (!DataInjection.bluetoothWakeSwitch || !hasPermission(ctx)) {
            stopMonitoring()
            return
        }
        if (registered || owners.isEmpty()) return
        val bt = (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter ?: return
        context = ctx
        adapter = bt
        try {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
            // Bluetooth broadcasts originate from the privileged Bluetooth process.
            ContextCompat.registerReceiver(ctx, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
            registered = true
            if (!bt.isEnabled) return
            val epoch = generation
            val listener = object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    synchronized(this@BluetoothConnectionMonitor) {
                        if (epoch != generation || !registered || !hasPermission(ctx)) {
                            try { bt.closeProfileProxy(profile, proxy) } catch (_: Exception) { }
                        } else {
                            proxies.put(profile, proxy)?.takeIf { it !== proxy }?.let { old ->
                                try { bt.closeProfileProxy(profile, old) } catch (_: Exception) { }
                            }
                            unavailableProfiles.remove(profile)
                            discovery.complete(profile)
                        }
                    }
                }
                override fun onServiceDisconnected(profile: Int) {
                    synchronized(this@BluetoothConnectionMonitor) {
                        if (epoch == generation) {
                            unavailableProfiles.add(profile)
                            discovery.complete(profile)
                            acl.clear()
                        }
                    }
                }
            }
            val profiles = mutableListOf(BluetoothProfile.A2DP, BluetoothProfile.HEADSET)
            if (Build.VERSION.SDK_INT >= 28) profiles.add(BluetoothProfile.HEARING_AID)
            if (Build.VERSION.SDK_INT >= 33) profiles.add(BluetoothProfile.LE_AUDIO)
            discovery.begin(profiles)
            for (profile in profiles) {
                try {
                    if (!bt.getProfileProxy(ctx, listener, profile)) discovery.complete(profile)
                } catch (_: IllegalArgumentException) { discovery.complete(profile) }
            }
        } catch (_: SecurityException) { stopMonitoring() }
    }
    private fun stopMonitoring() {
        generation++
        discovery.clear()
        if (registered) try { context?.unregisterReceiver(receiver) } catch (_: IllegalArgumentException) { }
        registered = false
        for ((profile, proxy) in proxies) try { adapter?.closeProfileProxy(profile, proxy) } catch (_: Exception) { }
        proxies.clear()
        unavailableProfiles.clear()
        acl.clear()
        context = null
        adapter = null
    }

    /** Zero means decide now. A positive delay is only for initial profile discovery, not
     * for disconnected devices. Permission loss, empty/unpaired selections and a detected
     * selected connection can all finish the wait immediately. Never authorizes a wake.
     */
    @Synchronized fun startupRetryDelayMillis(ctx: Context): Long {
        if (!DataInjection.bluetoothWakeSwitch) return 0L
        val selected = DataInjection.bluetoothWakeDevices
        if (selected.isEmpty()) return 0L
        val status = snapshot(ctx)
        if (!status.needsStartupWait(true, selected)) return 0L
        return (discovery.remainingMillis() + 50L).coerceAtMost(
            BluetoothStartupDiscovery.TIMEOUT_MILLIS + 50L)
    }

    /** Recheck permission, radio, pairing and supported live profiles at every wake. */
    @Synchronized fun snapshot(ctx: Context): Snapshot {
        val permitted = hasPermission(ctx)
        val manager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bt = manager?.adapter ?: return Snapshot(false, permitted, false)
        if (!permitted) { stopMonitoring(); return Snapshot(true, false, false) }
        ensureMonitoring(ctx.applicationContext)
        try {
            if (!bt.isEnabled) { acl.clear(); return Snapshot(true, true, false) }
            val devices = bt.bondedDevices.map { Device(it.address, it.name ?: it.address) }.sortedBy { it.name }
            val connected = acl.toMutableSet()
            for ((profile, proxy) in proxies) {
                if (profile !in unavailableProfiles) connected.addAll(proxy.connectedDevices.map { it.address })
            }
            connected.addAll(manager.getConnectedDevices(BluetoothProfile.GATT).map { it.address })
            connected.addAll(manager.getConnectedDevices(BluetoothProfile.GATT_SERVER).map { it.address })
            return Snapshot(true, true, true, devices, connected, discovery.remainingMillis() > 0L)
        } catch (_: SecurityException) {
            stopMonitoring()
            return Snapshot(true, false, false)
        } catch (_: RuntimeException) {
            // A restarting Bluetooth service cannot authorize a wake with old state.
            stopMonitoring()
            return Snapshot(true, permitted, false)
        }
    }
}
