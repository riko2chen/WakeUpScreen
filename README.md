<div align="center">

<img src="app/src/main/ic_launcher-web.png" width="120" />

# WakeUpScreen

**Your screen, awake when it matters.**

An open-source Android app that gently wakes your display the moment a notification arrives.
Maintained since 2019, with more than 10,000 users across 119 countries and regions.

[![Google Play](https://img.shields.io/badge/Google%20Play-Download-2dd4a8?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen)
[![GitHub](https://img.shields.io/badge/Source-GitHub-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/riko2chen/WakeUpScreen)
[![Website](https://img.shields.io/badge/Website-Visit-0ea5e9?style=for-the-badge&logo=googlechrome&logoColor=white)](https://riko2chen.github.io/WakeUpScreen/)
[![Changelog](https://img.shields.io/badge/Changelog-View-a855f7?style=for-the-badge)](docs/CHANGELOG.md)
[![License](https://img.shields.io/badge/License-GPLv3-a855f7?style=for-the-badge)](LICENSE)

[English](README.md) · [简体中文](README-zh.md) · [繁體中文](README-zh-TW.md) · [Italiano](README-it.md) · [日本語](README-ja.md) · [한국어](README-ko.md) · [ไทย](README-th.md)

</div>

---

## Features

| Feature | Description |
|---|---|
| **Instant Wake** | The screen lights up the moment a notification arrives. |
| **Pocket Mode** | Detects when the phone is in a pocket or bag and leaves the screen off. |
| **Repeat Reminder** | Wakes again every 5–60 minutes while notifications sit unread. |
| **Custom Screen-On Duration** | Holds the screen on for 5 to 30 seconds instead of the system timeout. |
| **App Filtering** | Choose exactly which apps can wake your screen. |
| **Attention Statistics** | Shows which apps wake the screen and never get looked at, counted on-device over 30 days. |
| **Notification Log** | Records every notification and which rule stopped it, with a live diagram of the check process. |
| **Dark Mode** | A dark interface that feels at home on any AMOLED display. |
| **No Internet** | Runs entirely on your device, collecting nothing and contacting no server. |
| **Lightweight** | Minimal footprint and negligible battery impact. |

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

## Screenshots

<div align="center">
<img src="screenshots/main-en.png" width="720" />
</div>

## Compatibility

- **Runs on**: Android 6.0 and later
- **Adapted for**: Android 16
- **Languages**: English, 简体中文, 繁體中文, Italiano, 日本語, 한국어, ไทย

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) — you are free to use,
study, modify and share it, and anything you distribute from it must stay open source under the same
terms.

---

<div align="center">

**WakeUpScreen** by [Riko Lab](mailto:symeonchen@gmail.com)

<sub>Previously hosted at <https://github.com/SymeonChen/WakeUpScreen> — that URL still redirects here. Same account, former username.</sub>

</div>
