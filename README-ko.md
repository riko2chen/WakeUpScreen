<div align="center">

<img src="app/src/main/ic_launcher-web.png" width="120" />

# 알림 화면 켜기（WakeUpScreen）

**중요한 순간에, 화면이 깨어납니다.**

알림이 도착하는 순간 화면을 부드럽게 켜 주는 오픈소스 안드로이드 앱입니다.
2019년부터 이어 온 프로젝트로, 119개 국가와 지역에서 10,000명이 넘는 사용자가 써 왔습니다.

[![Google Play](https://img.shields.io/badge/Google%20Play-다운로드-2dd4a8?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen)
[![GitHub](https://img.shields.io/badge/소스-GitHub-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/riko2chen/WakeUpScreen)
[![Website](https://img.shields.io/badge/웹사이트-방문-0ea5e9?style=for-the-badge&logo=googlechrome&logoColor=white)](https://riko2chen.github.io/WakeUpScreen/)
[![Changelog](https://img.shields.io/badge/변경_내역-보기-a855f7?style=for-the-badge)](docs/CHANGELOG-ko.md)
[![License](https://img.shields.io/badge/라이선스-GPLv3-a855f7?style=for-the-badge)](LICENSE)

[English](README.md) · [简体中文](README-zh.md) · [繁體中文](README-zh-TW.md) · [Italiano](README-it.md) · [日本語](README-ja.md) · [한국어](README-ko.md) · [ไทย](README-th.md)

</div>

---

## 주요 기능

| 기능 | 설명 |
|---|---|
| **즉시 켜짐** | 알림이 오는 순간 화면이 켜집니다. |
| **주머니 모드** | 주머니나 가방 안에 있는 것을 감지해 화면을 켜지 않습니다. |
| **반복 알림** | 읽지 않은 알림이 남아 있는 동안 5~60분마다 다시 켜집니다. |
| **화면 켜짐 시간 지정** | 시스템 화면 꺼짐 시간 대신 5~30초만 켜 두고 끕니다. |
| **앱 필터** | 어떤 앱이 화면을 켤 수 있는지 정확히 고릅니다. |
| **주의 통계** | 최근 30일 동안 화면은 켰지만 한 번도 확인되지 않은 앱을 기기 안에서 집계합니다. |
| **알림 로그** | 모든 알림의 켜짐 여부와 막힌 규칙을 기록하고, 검사 과정 도표를 실시간으로 보여 줍니다. |
| **다크 모드** | AMOLED 화면에 잘 어울리는 다크 인터페이스. |
| **인터넷 불필요** | 전부 기기 안에서 동작하며, 데이터를 모으지도 서버에 연결하지도 않습니다. |
| **가볍게** | 차지하는 자원이 최소한이고 배터리 영향도 거의 없습니다. |

## 사용 방법

```
1. 설치하고 권한 허용
   └─ 알림 접근 권한만 있으면 됩니다. 데이터가 기기를 벗어나는 일은 없습니다.

2. 앱 선택
   └─ 어떤 앱이 화면을 켤지 고릅니다. 중요한 메시지는 통과시키고
      나머지는 걸러 냅니다. 언제든 바꿀 수 있습니다.

3. 끝 — 평소처럼 지내세요
   └─ WakeUpScreen은 백그라운드에서 조용히 동작합니다. 알림이 오면 화면이 켜지고,
      주머니 안에서는 꺼진 채로 있습니다. 그게 전부입니다.
```

> **권한에 대하여.** 앱이 동작하는 데 필요한 권한은 알림 접근 하나뿐이며, 인터넷 권한은 전혀 요청하지
> 않습니다. 유일한 예외가 「화면 켜짐 시간 지정」입니다. Android 9 이상에서는 화면을 미리 끄는 일을
> 접근성 서비스만 할 수 있어서, 이 기능에 한해 접근성 권한을 요청합니다. 기본값은 꺼짐이고, 이 서비스는
> 화면 내용을 전혀 읽지 않으며, 나머지 기능은 권한 없이도 그대로 동작합니다.

## 그 밖의 기능

모두 자유롭게 켜고 끌 수 있습니다.

- **수면 모드** — 하루에 여러 개의 방해 금지 구간을 분 단위로 설정하고, 특정 요일에만 적용할 수도 있습니다
- **야간 미광** — 수면 구간에는 완전한 어둠 대신 어두운 붉은빛을 잠깐 보여 줍니다
- **배터리 부족 시 침묵** — 지정한 잔량 밑으로 떨어지고 충전 중이 아니면 화면을 켜지 않습니다
- **엎어 둘 때 침묵** — 화면을 아래로 두고 놓아둔 동안에는 켜지지 않습니다
- **방해 금지 감지** — 시스템 방해 금지 모드가 켜져 있는 동안에는 화면 켜기를 멈춥니다
- **충전 중에만** — 전원에 연결되어 있을 때만 화면을 켭니다
- **상시 알림 제외** — 내비게이션, 음악처럼 오래 남는 알림은 무시합니다
- **설정 백업** — 모든 설정을 JSON 파일로 내보내고 불러옵니다. 추가 권한은 필요 없습니다
- **켜짐 테스트** — 알림을 기다리지 않고 그 자리에서 화면 켜기나 반복 알림 한 번을 실행해 봅니다

## 스크린샷

<div align="center">
<img src="screenshots/main-en.png" width="720" />
</div>

## 지원 환경

- **최소 지원**: Android 6.0 이상
- **최신 대응**: Android 16
- **언어**: 한국어, English, 简体中文, 繁體中文, Italiano, 日本語, ไทย

## 기여

기여를 환영합니다. 이슈나 풀 리퀘스트를 편하게 남겨 주세요.

## 라이선스

이 프로젝트는 [GNU General Public License v3.0](LICENSE)에 따라 공개됩니다. 자유롭게 사용하고
연구하고 고치고 나눌 수 있지만, 여기서 파생되어 배포되는 것도 같은 조건으로 오픈소스여야 합니다.

---

<div align="center">

**WakeUpScreen** by [Riko Lab](mailto:symeonchen@gmail.com)

<sub>이전 저장소: <https://github.com/SymeonChen/WakeUpScreen> — 이 주소는 지금도 이곳으로 연결됩니다. 같은 계정의 예전 사용자 이름입니다.</sub>

</div>
