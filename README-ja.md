<div align="center">

<img src="app/src/main/ic_launcher-web.png" width="120" />

# 通知画面点灯（WakeUpScreen）

**大事なときに、画面が目を覚ます。**

通知が届いた瞬間にディスプレイをそっと点灯させる、オープンソースの Android アプリ。
2019 年から開発を続け、119 の国と地域で 10,000 人を超えるユーザーに使われています。

[![Google Play](https://img.shields.io/badge/Google%20Play-ダウンロード-2dd4a8?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen)
[![GitHub](https://img.shields.io/badge/ソース-GitHub-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/riko2chen/WakeUpScreen)
[![Website](https://img.shields.io/badge/公式サイト-訪問-0ea5e9?style=for-the-badge&logo=googlechrome&logoColor=white)](https://riko2chen.github.io/WakeUpScreen/)
[![Changelog](https://img.shields.io/badge/変更履歴-見る-a855f7?style=for-the-badge)](docs/CHANGELOG-ja.md)
[![License](https://img.shields.io/badge/ライセンス-GPLv3-a855f7?style=for-the-badge)](LICENSE)

[English](README.md) · [简体中文](README-zh.md) · [繁體中文](README-zh-TW.md) · [Italiano](README-it.md) · [日本語](README-ja.md) · [한국어](README-ko.md) · [ไทย](README-th.md)

</div>

---

## 主な機能

| | 機能 | 説明 |
|---|---|---|
| :bell: | **即時点灯** | 通知が届いた瞬間に画面が点灯します。 |
| :sun_with_face: | **ポケットモード** | ポケットやバッグの中にあることを検知して、画面を消したままにします。 |
| :repeat: | **繰り返しリマインド** | 未読の通知が残っている間、5〜60 分ごとに再点灯します。 |
| :hourglass_flowing_sand: | **点灯時間のカスタマイズ** | システムの画面消灯時間ではなく、5〜30 秒だけ点灯して消します。 |
| :mag: | **アプリの絞り込み** | どのアプリに画面を点けさせるかを細かく選べます。 |
| :chart_with_upwards_trend: | **注目度の統計** | 過去 30 日間で画面を点けたのに一度も見られなかったアプリを端末内で集計します。 |
| :clipboard: | **通知ログ** | すべての通知について点灯の有無とどのルールで止まったかを記録し、チェック処理の図もリアルタイムで表示します。 |
| :new_moon: | **ダークモード** | AMOLED ディスプレイに馴染むダーク表示。 |
| :closed_lock_with_key: | **通信不要** | すべて端末内で完結し、データ収集もサーバー通信もありません。 |
| :zap: | **軽量** | 占有リソースはごくわずかで、バッテリーへの影響もほとんどありません。 |

## 使い方

```
1. インストールして権限を許可
   └─ 必要なのは通知へのアクセスだけ。データが端末から出ることはありません。

2. アプリを選ぶ
   └─ どのアプリに画面を点けさせるかを選択。大事なメッセージは通し、
      それ以外は遮ります。いつでも変更できます。

3. あとはふだんどおりに
   └─ WakeUpScreen はバックグラウンドで静かに動きます。通知が来れば点灯し、
      ポケットの中では消えたまま。それだけです。
```

> **権限について。** アプリの動作に必要な権限は通知へのアクセスだけで、ネットワーク権限は一切要求しません。
> 唯一の例外が「点灯時間のカスタマイズ」です。Android 9 以降では画面を早めに消せるのはユーザー補助
> サービスだけなので、この機能に限ってユーザー補助の許可を求めます。初期状態はオフ、サービスは画面の
> 内容を一切読み取らず、他の機能は許可しなくても通常どおり動きます。

## その他の機能

いずれも自由にオン・オフできます。

- **おやすみモード** — 1 日に複数の静音時間帯を分単位で設定でき、曜日を限定することもできます
- **ナイトグロー** — おやすみ時間帯では、真っ暗のかわりに一瞬だけ暗い赤色の微光を表示します
- **バッテリー低下時の静音** — 指定した残量を下回り、かつ充電していないときは点灯しません
- **伏せ置き時の静音** — 画面を下にして置いている間は消灯したままにします
- **サイレントモードの検知** — システムのサイレントモード中は点灯を停止します
- **充電中のみ** — 電源に接続しているときだけ点灯します
- **常駐通知の除外** — ナビ・音楽など、長く残る通知を無視します
- **設定のバックアップ** — すべての設定を JSON ファイルで書き出し・読み込みできます。追加の権限は不要です
- **点灯テスト** — 通知を待たずに、その場で点灯や繰り返しリマインドを 1 回試せます

## スクリーンショット

<div align="center">
<img src="screenshots/main-en.png" width="720" />
</div>

## 対応環境

- **動作環境**: Android 6.0 以降
- **最新対応**: Android 16
- **言語**: 日本語, English, 简体中文, 繁體中文, Italiano, 한국어, ไทย

## コントリビュート

貢献を歓迎します。Issue や Pull Request をお気軽にどうぞ。

## ライセンス

本プロジェクトは [GNU General Public License v3.0](LICENSE) のもとで公開されています。自由に使用・
研究・改変・配布できますが、そこから派生して配布するものも同じ条件でオープンソースにする必要があります。

---

<div align="center">

**WakeUpScreen** by [Riko Lab](mailto:symeonchen@gmail.com)

<sub>旧リポジトリ: <https://github.com/SymeonChen/WakeUpScreen> — この URL は現在もこちらへリダイレクトします。同一アカウントの旧ユーザー名です。</sub>

</div>
