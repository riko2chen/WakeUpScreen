<div align="center">

<img src="app/src/main/ic_launcher-web.png" width="120" />

# WakeUpScreen

**Your screen, awake when it matters.**

An open-source Android app that gently wakes your display the moment a notification arrives.
No cloud, no clutter, no compromise.

[![Google Play](https://img.shields.io/badge/Google%20Play-Download-2dd4a8?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen)
[![GitHub](https://img.shields.io/badge/Source-GitHub-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/riko2chen/WakeUpScreen)
[![Website](https://img.shields.io/badge/Website-Visit-0ea5e9?style=for-the-badge&logo=googlechrome&logoColor=white)](https://riko2chen.github.io/WakeUpScreen/)
[![Changelog](https://img.shields.io/badge/Changelog-View-a855f7?style=for-the-badge)](docs/CHANGELOG.md)
[![License](https://img.shields.io/badge/License-GPLv3-a855f7?style=for-the-badge)](LICENSE)

[English](README.md) · [中文](README-zh.md) · [Italiano](README-it.md)

</div>

---

## Features

| | Feature | Description |
|---|---|---|
| :bell: | **Instant Wake** | Your screen lights up the moment a notification arrives. Never miss what matters while your phone sits on the desk. |
| :sun_with_face: | **Pocket Mode** | Intelligently detects when your phone is in a pocket or bag, and stays off. Saves battery where it counts. |
| :repeat: | **Repeat Reminder** | Missed it the first time? The screen wakes again every 5–60 minutes while notifications sit unread, and stops the moment you clear them. |
| :hourglass_flowing_sand: | **Custom Screen-On Duration** | Choose exactly how long the screen stays on — 5, 10, 15 or 30 seconds — instead of leaving it to the system screen timeout. |
| :mag: | **App Filtering** | Choose exactly which apps can wake your screen. Full control over what deserves your attention. |
| :chart_with_upwards_trend: | **Attention Statistics** | See which apps wake your screen and never get looked at, counted on-device over the last 30 days, and blacklist the worst offenders in one tap. |
| :clipboard: | **Notification Log** | Every notification, whether it woke the screen or not — and exactly which rule stopped it, alongside a live diagram of the whole check process. |
| :new_moon: | **Dark Mode** | A beautiful dark interface that feels at home on any AMOLED display. Easy on the eyes, easy on the battery. |
| :closed_lock_with_key: | **No Internet** | Runs entirely on your device. Zero data collected, zero servers contacted. Your privacy is absolute. |
| :zap: | **Lightweight** | Minimal footprint, negligible battery impact. Built in Kotlin for native performance that just works. |

## How It Works

```
1. Install & Grant Permission
   └─ Only notification access needed. Your data never leaves your device.

2. Choose Your Apps
   └─ Select which apps wake your screen. Let important messages through, filter the noise.

3. That's It — Live Your Life
   └─ WakeUpScreen runs silently in the background. Screen lights up on notification,
      stays dark in your pocket. Simple as that.
```

> **On permissions.** Notification access is the only one the app needs to do its job, and it
> requests no internet permission at all. Custom Screen-On Duration is the single exception: turning
> the screen off early is something only an accessibility service can do on Android 9+, so that one
> feature asks for an accessibility grant. It is off by default, the service reads no screen content,
> and everything else works without it.

## More Features

All of these can be turned on or off:

- **Sleep Mode** — several quiet windows a day, set to the minute, optionally limited to chosen weekdays
- **Night Glow** — during a sleep window, show a dim red glow for a moment instead of full darkness
- **Low Battery Silence** — below a chosen battery level and not charging, stop waking the screen
- **Face-down Silence** — while the phone lies screen-down, leave the screen off
- **Do Not Disturb Detection** — pause waking while the system DND is on
- **Charging-Only Mode** — wake the screen only while the phone is plugged in
- **Ongoing Notification Filtering** — ignore navigation, music and other long-lived notifications
- **Settings Backup** — export and import every setting as a JSON file, no extra permissions
- **Wake Test** — trigger a wake, or one repeat reminder, on the spot instead of waiting for a notification
- **In-App Changelog** — after an update, a short summary of what changed, covering every version skipped

## Screenshots

<div align="center">
<img src="screenshots/main-en.png" width="720" />
</div>

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Min SDK**: Android 6.0 (API 23), targets Android 16 (API 36)
- **Architecture**: MVVM
- **Languages**: English, 简体中文, 繁體中文, Italiano, 日本語, 한국어, ไทย

## Building

```bash
git clone https://github.com/riko2chen/WakeUpScreen.git
cd WakeUpScreen
./gradlew assembleDebug
```

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

---

<div align="center">

**WakeUpScreen** by [Riko Lab](mailto:symeonchen@gmail.com)

<sub>Previously hosted at <https://github.com/SymeonChen/WakeUpScreen> — that URL still redirects here. Same account, former username.</sub>

</div>
