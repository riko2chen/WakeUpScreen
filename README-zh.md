* [English Version](https://github.com/SymeonChen/WakeUpScreen/blob/master/README.md)
* [Italian Version](https://github.com/SymeonChen/WakeUpScreen/blob/master/README-it.md)
* [中文版](https://github.com/SymeonChen/WakeUpScreen/blob/master/README-zh.md)

# 通知亮屏（WakeUpScreen）

![](screenshots/introduction_zh.png)

<img src="app/src/main/ic_launcher-web.png" align="left" width="200" hspace="10" vspace="10">
</br>
这是一个可以使手机在接收到通知时点亮屏幕的 Android APP。
主要面向三星用户，也同样兼容其他设备。
</br>
<div style="display:flex;" >
<a href="https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen">
    <img alt="Get it on Google Play"
        height="80"
        src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" />
</a>
</div>
</br></br>

## 截图

![](screenshots/main-zh.jpg)

## 特点

三星的消息通知策略更倾向于 Always on Display，本应用则倾向于在平常关闭屏幕，收到通知时再点亮，类似于 iOS 以及 MIUI 等系统的表现形式。

与类似应用相比，本应用有三个重要特点：
1. 开源。遵循GPL协议，所有代码都是公开的。
2. 无需网络连接。应用不申请网络权限，让使用者安心放心。
3. 无广告。因为本应用完全是针对我自己的需求开发，顺便分享给大家，没有盈利压力，因此不加广告。

除了这些关键点，本应用还提供但不限于以下功能（均可自由开启或关闭）：
1. 口袋模式。当手机放在口袋里或倒扣在桌子上时，即使收到通知也不会亮屏。
2. 分应用过滤。推荐使用，将需要亮屏的应用加入白名单，避免频繁亮屏，同样支持黑名单。
3. 自定义亮屏时间。（本功能在部分设备上不可用）
4. 免打扰模式侦测。当手机开启免打扰功能时，将自动暂停亮屏功能。
5. 睡眠模式。可以自由定义夜间暂停亮屏的时间段。
6. 持续通知优化。对于长时间驻留在通知栏的消息，比如导航、音乐播放，能够自动忽略其导致的亮屏行为。