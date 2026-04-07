# Changelog

All notable changes to WakeUpScreen are documented here, organized by version.

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
