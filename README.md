<div align="center">

<img src="app/src/main/ic_launcher-web.png" width="120" />

# WakeUpScreen

**Your screen, awake when it matters.**

An open-source Android app that gently wakes your display the moment a notification arrives.
No cloud, no clutter, no compromise.

[![Google Play](https://img.shields.io/badge/Google%20Play-Download-2dd4a8?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen)
[![GitHub](https://img.shields.io/badge/Source-GitHub-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/SymeonChen/WakeUpScreen)
[![License](https://img.shields.io/badge/License-GPLv3-a855f7?style=for-the-badge)](LICENSE)

[English](README.md) · [中文](README-zh.md) · [Italiano](README-it.md)

</div>

---

## Features

| | Feature | Description |
|---|---|---|
| :bell: | **Instant Wake** | Your screen lights up the moment a notification arrives. Never miss what matters while your phone sits on the desk. |
| :sun_with_face: | **Pocket Mode** | Intelligently detects when your phone is in a pocket or bag, and stays off. Saves battery where it counts. |
| :mag: | **App Filtering** | Choose exactly which apps can wake your screen. Full control over what deserves your attention. |
| :new_moon: | **Dark Mode** | A beautiful dark interface that feels at home on any AMOLED display. Easy on the eyes, easy on the battery. |
| :closed_lock_with_key: | **No Internet** | Runs entirely on your device. Zero data collected, zero servers contacted. Your privacy is absolute. |
| :zap: | **Lightweight** | Minimal footprint, negligible battery impact. Built in Kotlin for native performance that just works. |

## How It Works

```
1. Install & Grant Permission
   └─ Only notification access needed. No other permissions. Your data never leaves your device.

2. Choose Your Apps
   └─ Select which apps wake your screen. Let important messages through, filter the noise.

3. That's It — Live Your Life
   └─ WakeUpScreen runs silently in the background. Screen lights up on notification,
      stays dark in your pocket. Simple as that.
```

## Screenshots

<div align="center">
<img src="screenshots/main-en.png" width="720" />
</div>

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Min SDK**: Android 6.0 (API 23)
- **Architecture**: MVVM

## Building

```bash
git clone https://github.com/SymeonChen/WakeUpScreen.git
cd WakeUpScreen
./gradlew assembleDebug
```

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

---

<div align="center">

**WakeUpScreen** by [Riko Studio](mailto:symeonchen@gmail.com)

*Built in the open. Transparency is not optional.*

</div>
