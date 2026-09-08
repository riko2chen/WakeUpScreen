# Selected Bluetooth device wake condition

Under **Wake Rules → Bluetooth wake condition**, enable the rule and select one or more paired devices. One selected connected device is enough. The default is off. This gate precedes the sleep gate and is part of the shared condition chain used by ordinary notifications and repeat reminders, so Night Glow cannot bypass it.

With the rule on, no selection, no adapter, Bluetooth off, missing/revoked Nearby devices permission, unpaired selections, and undetected connections all block wakes. The screen distinguishes these states and lets users remove unavailable selections or clear the entire selection. Permission is requested only by explicit opt-in on Android 12 or later; denial leaves the rule enabled and blocking, with a link to app permissions. Restoring a backup never requests permission.

The listener owns connection monitoring while connected. The settings screen owns it while visible. Owners share one receiver and profile-proxy set; the last owner releases them, and disabling the rule or discovering lost permission clears connection state. A generation token closes late proxy callbacks from earlier registrations. Listener reconnection reinitializes snapshots after process death or rebinding.

Startup and wake-time checks use public A2DP and Headset profile proxies, Hearing Aid on API 29+, LE Audio on API 33+, and BluetoothManager GATT/GATT_SERVER snapshots. Protected ACL connection broadcasts track later connections for other types. Paired devices alone never count as connected. Existing connections on profiles without public snapshot APIs may remain undetected until reconnecting while monitoring is active. The screen explains this limitation; unknown connections block wakes. No scanning, location permission, hidden APIs, or reflection are used.

The Bluetooth receiver uses `ContextCompat.RECEIVER_EXPORTED`: broadcasts originate from the privileged Bluetooth process. Its filter includes only protected Bluetooth ACL, bond, and adapter-state actions.

Public API references: [Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions), [BluetoothManager connection snapshots](https://developer.android.com/reference/android/bluetooth/BluetoothManager#getConnectedDevices(int)), [BluetoothAdapter profile proxies](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#getProfileProxy(android.content.Context,%20android.bluetooth.BluetoothProfile.ServiceListener,%20int)).

## Validation

Policy unit tests cover disabled behavior, any-selected matching, paired-versus-connected distinction, empty selection, unpaired restored selection, permission/radio loss overriding cached connections, and disconnect/reconnect. Instrumentation tests cover persistence and backup import, malformed addresses, real-device empty-selection blocking, missing context, and log-chain ordering before sleep. The existing chain-order unit test includes the new gate.

Build and instrumentation are coordinated by the parent task; no independent Gradle or emulator run was started in this worktree.

Physical hardware checks still required:

- Enable with permission denied, then grant it through app settings; revoke it again while monitoring.
- Connect paired audio and BLE devices before starting/rebinding the listener; confirm selection status without reconnecting.
- Select two devices; disconnect each in turn. Wakes remain allowed until the last selected connected device disconnects.
- Connect only an unselected device; unpair a selected device; toggle the Bluetooth radio off/on.
- During a sleep window with Night Glow enabled, confirm a blocked Bluetooth condition suppresses notifications and repeat reminders.
- Repeat on API 23–30 (legacy Bluetooth permission) and API 31+ (Nearby devices runtime permission).
- Inspect an unsupported-profile device already connected before monitoring: the UI must report undetected rather than imply all paired devices are connected.

## Bounded startup discovery

Profile proxies load asynchronously. Pending initialization now has a fixed 1.5-second deadline and is distinct from a known disconnected state. A selected paired device can finish this wait as soon as its connection is detected, without waiting for unrelated profile callbacks. Failed requests, radio/permission loss, empty selections, and unpaired selections do not cause a wait.

Notifications arriving during initialization are retained in one short-lived batch. Updates replace their payload without extending the wait. On completion or timeout, the listener fetches active notifications again, drops dismissed notifications, reevaluates all rules, and produces at most one wake or Night Glow. A timeout never allows a wake by itself, and a later connection does not replay the old batch. Listener disconnect/destruction cancels the batch.

A repeat reminder blocked solely by pending Bluetooth discovery schedules a short retry near that deadline without consuming a round, rather than waiting the full configured interval. The next alarm checks active notifications and every wake rule again. This uses `ReminderScheduler.scheduleRetry(context, delayMillis)`; integration with the reliability branch must use its persisted deadline and normal alarm replacement logic. Android may still defer an inexact alarm during Doze.

Added deterministic discovery/queue tests cover async readiness, fixed timeout under updates, canceled work, denied/unpaired/empty states, fresh payloads, dismissal, changed rules, and one-wake batch coalescing. These tests await the parent task’s coordinated build.
