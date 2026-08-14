<div align="center">

<img src="app/src/main/ic_launcher-web.png" width="120" />

# 通知亮屏（WakeUpScreen）

**屏幕，在重要时刻为你亮起。**

一款开源 Android 应用，收到通知时自动点亮屏幕。
无云服务、无冗余、零妥协。

[![Google Play](https://img.shields.io/badge/Google%20Play-下载-2dd4a8?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen)
[![GitHub](https://img.shields.io/badge/源码-GitHub-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/riko2chen/WakeUpScreen)
[![Website](https://img.shields.io/badge/官网-访问-0ea5e9?style=for-the-badge&logo=googlechrome&logoColor=white)](https://riko2chen.github.io/WakeUpScreen/)
[![Changelog](https://img.shields.io/badge/更新日志-查看-a855f7?style=for-the-badge)](docs/CHANGELOG-zh.md)
[![License](https://img.shields.io/badge/许可证-GPLv3-a855f7?style=for-the-badge)](LICENSE)

[English](README.md) · [中文](README-zh.md) · [Italiano](README-it.md)

</div>

---

## 功能特性

| | 功能 | 描述 |
|---|---|---|
| :bell: | **即时亮屏** | 收到通知的瞬间屏幕自动亮起。手机放在桌上也不会错过重要信息。 |
| :sun_with_face: | **口袋模式** | 智能检测手机是否在口袋或包中，自动保持息屏。省电从细节做起。 |
| :repeat: | **重复提醒** | 第一次没看到？只要通知还没处理，屏幕会每隔 5～60 分钟再次亮起，通知一被清除就自动停止。 |
| :hourglass_flowing_sand: | **自定义亮屏时长** | 自己决定屏幕亮多久 —— 5、10、15 或 30 秒，不再交给系统的「屏幕超时」。 |
| :mag: | **应用筛选** | 精确选择哪些应用可以亮屏。完全掌控什么值得你的关注。 |
| :chart_with_upwards_trend: | **注意力统计** | 完全在本地统计最近 30 天里「点亮了屏幕却从没被看一眼」的应用，一键把最吵的加入黑名单。 |
| :clipboard: | **通知日志** | 每一条通知都有记录，亮没亮屏、被哪条规则拦下，一目了然，还配了一张实时的检查流程图。 |
| :new_moon: | **深色模式** | 精美的深色界面，完美适配 AMOLED 屏幕。护眼又省电。 |
| :closed_lock_with_key: | **无需网络** | 完全在设备本地运行。零数据采集，零服务器连接。你的隐私得到绝对保障。 |
| :zap: | **轻量级** | 极小的资源占用，几乎无感的电量消耗。基于 Kotlin 开发，原生性能开箱即用。 |

## 设计理念

三星的消息通知策略更倾向于 Always on Display，而本应用则倾向于在平常关闭屏幕，收到通知时再点亮，类似于 iOS 以及 MIUI 等系统的表现形式。

与类似应用相比，本应用有三个核心优势：
- **开源** — 遵循 GPL 协议，所有代码完全公开
- **无需网络** — 不申请网络权限，让使用者安心放心
- **无广告** — 纯粹为需求而生，没有盈利压力

## 使用方法

```
1. 安装并授权
   └─ 仅需通知访问权限来监听传入通知，数据永远不会离开你的设备。

2. 选择应用
   └─ 选择哪些应用可以唤醒屏幕。放行重要消息，过滤无关干扰。随时可调整。

3. 就这样，尽情生活
   └─ WakeUpScreen 在后台静默运行。收到通知时屏幕自动亮起，
      手机在口袋里时保持息屏。就这么简单。
```

> **关于权限。** 应用工作只需要通知访问权限，且完全不申请网络权限。唯一的例外是「自定义亮屏时长」：
> 在 Android 9 及以上，提前熄屏只有无障碍服务能做到，所以这一个功能会请求无障碍授权。它默认关闭，
> 该服务不读取任何屏幕内容，其余功能不授权也照常工作。

## 更多功能

以下功能均可自由开启或关闭：

- **睡眠模式** — 一天可设多个免打扰时段，精确到分钟，还能只在指定星期生效
- **夜间微光** — 睡眠时段内，用一瞬间的暗红微光代替全黑
- **低电量静默** — 电量低于设定值且未充电时，不再亮屏
- **面朝下静默** — 手机屏幕朝下放置时，保持息屏
- **免打扰侦测** — 手机开启免打扰时，自动暂停亮屏功能
- **仅充电时** — 只在手机插着电的时候亮屏
- **持续通知优化** — 自动忽略导航、音乐等长驻通知的亮屏行为
- **设置备份** — 全部设置导出、导入为 JSON 文件，无需额外权限
- **唤醒测试** — 不用等通知，当场触发一次亮屏，或者一轮重复提醒
- **应用内更新日志** — 更新后首次启动会列出改了什么，跳过的版本也一并交代

## 截图

<div align="center">
<img src="screenshots/main-zh.png" width="720" />
</div>

## 技术栈

- **语言**: Kotlin
- **界面**: Jetpack Compose
- **最低版本**: Android 6.0 (API 23)，目标版本 Android 16 (API 36)
- **架构**: MVVM
- **界面语言**: 简体中文、繁體中文、English、Italiano、日本語、한국어、ไทย

## 构建

```bash
git clone https://github.com/riko2chen/WakeUpScreen.git
cd WakeUpScreen
./gradlew assembleDebug
```

## 贡献

欢迎贡献！可以提交 Issue 或 Pull Request。

## 许可证

本项目基于 [GNU 通用公共许可证 v3.0](LICENSE) 开源。

---

<div align="center">

**WakeUpScreen** by [Riko Lab](mailto:symeonchen@gmail.com)

<sub>原仓库地址：<https://github.com/SymeonChen/WakeUpScreen> —— 该链接仍可访问并自动跳转至当前地址。同一账号，仅为曾用名。</sub>

</div>
