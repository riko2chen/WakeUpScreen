# Changelog

All notable changes to WakeUpScreen are documented here, organized by version.

---

## [3.2.0]

### Added
- Sleep mode accepts several windows, added and removed one by one, so a night window and an afternoon nap can coexist
- Sleep windows are set to the minute instead of the full hour
- The sleep time page shows the day as a 24-hour ring, with sleep windows greyed out, the current moment marked, and ticks at 0, 6, 12 and 18
- Adding a window checks it against the existing ones, naming the window it would collide with instead of silently accepting an overlap

### Changes
- A sleep window runs from its start up to but not including its end, so 02:00 - 04:00 stops at 03:59. Two windows can therefore meet end to end, one starting exactly where another finishes
- An existing single window is converted automatically on upgrade and keeps behaving the same
- "Optimize Ongoing Notify" is now "Block Ongoing Notifications" and "Radical Ongoing Detect" is now "Block Non-clearable Notifications". Both carry a "?" button explaining what each one stops and how they differ
- Settings are regrouped into General, Advanced, Diagnostics and About. General holds language and the new dark mode; Advanced holds the notification check process and the wake rules page; Diagnostics holds the wake test and a direct entry to the log
- New dark mode setting with three options: follow system, light, dark. It defaults to following the system, so appearance does not change on upgrade
- Screen-on duration and the app filter (mode, whitelist, blacklist) moved from the settings home into the wake rules page, which now covers everything that decides whether the screen lights up
- Several entries were renamed to say what they actually do: "Advanced Setting" is now "Wake Rules", "Block Chain" is now "Notification Check Process", "Function Test" is now "Wake Test", and "Current Mode" is now "Filter Mode". Only the labels changed, every setting keeps its value

### Fixes
- Fixed "Radical Ongoing Detect" (Settings → Wake Rules) writing to the wrong preference key: turning it on or off silently flipped the plain "Ongoing Detect" setting instead, and the radical switch itself was never saved — it reverted to on every time the page was reopened. Both switches are now independent and persist correctly

### Notes
- Radical Ongoing Detect was effectively always on before this fix and stays on by default, so notification filtering does not change on upgrade
- If you ever toggled the radical switch, the plain "Ongoing Detect" setting may have been changed without your knowing. Both switches are worth a quick check under Settings → Wake Rules

---

## [3.1.1]

### Changes
- Now targets Android 16 (API 36). Verified on an Android 16 system image: precise screen-on windows, the accessibility lock-screen action and the permission-free fallback all behave exactly as before
- Secondary pages now use Android's predictive back animation instead of the app's own slide transition. This comes with targeting Android 16 and is a visual change only — back navigation itself is unchanged

### Notes
- No new permissions, no functional changes
- Verified on a device image using the new 16 KB memory page size

---

## [3.1.0]

### Features
- Added "Repeat Reminder" (Settings → Advanced Setting): while notifications sit unread, the screen wakes again every 5–60 minutes and stops as soon as they are cleared. The interval and the maximum number of reminders per batch are configurable, and sleep mode, Do Not Disturb, pocket mode, charging-only mode and the app filter all still take priority. Ongoing notifications never count as unread
- Reminder wakes are recorded in the notification log, and Function Test can run one reminder immediately instead of waiting out the interval
- Added "Custom screen-on duration" (Settings → Custom screen-on duration): the screen is held on for 3, 5, 10, 15 or 30 seconds and then turned off, instead of being handed back to the system screen timeout. Turning the screen off uses the system's own lock-screen action through an optional accessibility service, which reads no screen content

### Changes
- Custom screen-on duration is off by default and does nothing while it is off, so existing installs behave exactly as before — the old implementation released its wake lock immediately and never applied the configured value either

### Notes
- No new permissions. Reminders use inexact alarms, so Doze may delay one by a few minutes
- A custom screen-on window is cancelled early if the device is unlocked or the screen goes off, and never blanks the display during a call

---

## [3.0.6]

### Changes
- Removed the `FOREGROUND_SERVICE_SPECIAL_USE` permission. Waking the screen relies on the notification listener, which the system keeps bound automatically, so the special-use foreground service was not actually required for core behavior.
- Removed the optional "persistent notification (keep service alive)" setting, which was the only feature that used that permission. On aggressive battery-management devices, please keep the app exempt from battery optimization instead.

---

## [3.0.5]

### Features
- Added a "Check for updates" page (Settings → About) with Google Play, F-Droid and GitHub options, and the current installed version. No internet permission is required — it simply opens the chosen store or page.

### Fixes
- Fixed edge-to-edge / safe-area layout on Android 15+: back buttons are no longer unreachable, titles are no longer hidden behind the status bar or camera cutout, and the bottom navigation and lists now clear the gesture bar.

---

## [3.0.4]

### Fixes
- Fixed notification log always showing "Unknown reason" in release builds — the block reason is now resolved from a stable identifier instead of the obfuscated class name, so the real reason (DND, charging, sleep mode, etc.) is displayed correctly
- Persistent notification toggle now takes effect immediately, without needing to restart the app or service

### Improvements
- Any unrecognized block reason is now shown as-is in the log instead of collapsing into "Unknown reason", so no information is lost when diagnosing issues

---

## [3.0.3]

### Changes
- Added F-Droid fastlane metadata (descriptions, screenshots, changelogs in English, Chinese, Italian)
- Moved CHANGELOG files into `docs/` directory
- Added changelog viewer page on the website
- Fixed README: Min SDK corrected to Android 6.0 (API 23) to match the code

---

## [3.0.2]

### Major Changes
- Complete UI overhaul rebuilt with Jetpack Compose
- Brand new visual design system (Material 3 Periwinkle Bloom theme)
- New pages: notification log, function test, feedback
- Multi-language support expanded
- Android 14/15 compatibility

### UI & Design
- Major artistic UI redesign — dark forest dawn theme
- Major artistic UI redesign — Periwinkle Bloom M3 theme
- Rewrote Home and Settings screens with Jetpack Compose
- Rewrote all secondary pages with Jetpack Compose
- Migrated dialogs to Compose
- Enhanced SettingRow with customizable trailing icons and improved app icon handling
- UI review and optimization pass

### Features
- Added charging-only filter to advanced settings
- Added feedback page
- Added function test page
- Added notification log page with channel info (importance, sound, vibration)
- Multi-language support added

### Infrastructure
- Upgraded Gradle to 8.9, AGP to 8.7.3, Kotlin to 2.0.21
- Migrated to JDK 17
- Migrated deprecated APIs
- Added Android 14/15 compatibility
- Upgraded MMKV to 2.4.0 (16KB page size support)
- Added GitHub Actions CI/CD workflow with APK signing and auto-release
- Updated license from MIT to GPLv3

---

## [2.2.2]

### Features
- Added POST_NOTIFICATIONS runtime permission for Android 13+

### Docs
- Added privacy policy document

---

## [2.2.1]

### Refactor
- Migrated all modules from synthetic to View Binding
- Added ViewBinding extension utilities

### Other
- Dependency updates

---

## [2.2.0]

### Features
- Added dark mode support
- Added more fine-grained dark mode configurations

### Performance
- Enabled R8 code shrinking
- Removed Play Core library to reduce APK size

### Other
- Dependency upgrades

---

## [2.1.1]

### Changes
- Changed default value for persistent notification setting

---

## [2.1.0]

### Features
- Added option to hide/close the persistent foreground notification
- Optimized main activity reset entry point

---

## [2.0.0]

### Major Changes
- Migrated to Foreground Service to prevent the system from killing the listener

### Features
- Auto rebind when notification listener service disconnects
- Removed embedded Realm database; simplified data layer
- Refactored multilingual string handling into a single unified location

---

## [1.9.0]

### Changes
- Added cooldown time limit to the in-app review prompt to avoid repeated requests

---

## [1.8.0]

### Features
- Integrated Google in-app review API
- Moved the rating entrance to a more prominent position

### Other
- Dependency upgrades and unused code cleanup

---

## [1.7.0]

### Refactor
- Refactored conditional filter rules for notification-triggered screen awakening

---

## [Earlier Versions]

### Features Added Over Time
- Whitelist / app filter list: allow specific apps to trigger wake-up
- Pocket mode: prevent screen wake when device is in pocket (proximity sensor)
- Sleep mode: configurable quiet hours
- DND mode awareness
- Battery optimization guidance and quick navigation
- Persistent notification with foreground service
- Debug / log page for diagnosing notification events
- Language switching support
- Bottom navigation structure
- Custom toggle switches
- ViewModel + LiveData architecture
- Proximity sensor optimization
