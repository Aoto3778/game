# ZERO SUM

カードを1枚取るたび、残り4枚が敵の選択肢になる。完全オフラインのゼロサム・ドラフト式ローグライク・デッキビルダーです。

## 特徴

- 3クラス、基本カード100種と全アップグレード
- レリック40種、敵28種、分岐イベント25種、ルールを書き換えるボス4種
- 3幕・12〜13ノードのseed固定分岐マップ
- 取らなかったカードを戦闘ごとに利用する敵AI
- 昇華度0〜20、日付固定のデイリーチャレンジ、30実績、永続統計
- 戦闘途中を含む自動セーブ／レジューム
- Canvasだけの描画と、AudioTrackによる矩形波／三角波のリアルタイム効果音
- 日本語／英語、縦持ち片手操作、ダークテーマ固定

ネットワーク権限、広告、課金、アナリティクス、画像・音声アセットは一切ありません。

## 画面構成

```text
┌────────────────────────────┐
│ ACT / HP / GOLD            │
├────────────────────────────┤
│       enemy + intent       │
│        damage pop          │
├────────────────────────────┤
│ status / turn / block      │
│                            │
│  [card] [card] [card] →    │  上へドラッグしてプレイ
├────────────────────────────┤
│ ENERGY         [END TURN]  │  主操作は画面下2/3
└────────────────────────────┘
```

タイトル → 分岐マップ → 戦闘 → 5択ドラフトを繰り返します。ドラフトで選ばなかった4枚は敵プールに入り、以降の敵が強度に応じて2／4／6枚を使用します。

## ビルド

必要環境は JDK 17 と Android SDK（API 35以上）です。

```bash
./gradlew :core:test :app:assembleDebug
./gradlew :sim:run --args="--runs 2000 --seed 1"
```

Windows:

```powershell
.\gradlew.bat :core:test :app:assembleDebug
.\gradlew.bat :sim:run --args="--runs 2000 --seed 1"
```

APK は `app/build/outputs/apk/debug/app-debug.apk` に生成されます。難読化なし・debug署名のAPKが配布物です。

## インストール

端末でUSBデバッグを有効にし、Android SDKの `adb` からインストールします。

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Android 8.0（API 26）以上に対応します。通信は行わず、セーブと統計は端末内のDataStoreだけに保存されます。

## モジュール

- `:core` — Android依存ゼロ。immutable state、reducer、コンテンツ、AI、乱数、セーブ形式、実績
- `:sim` — Android依存ゼロ。3種Botによる大量自動対戦と統計
- `:app` — Composeによる描画と入力、DataStore、AudioTrack、ハプティクス

ゲームルールの更新口は `reduce(state, action)` だけです。同じ初期状態とAction列は必ず同じ最終状態になります。

## 検証

実測値と調整履歴は [BALANCE.md](BALANCE.md)、環境上実行できなかった項目と代替案は [BLOCKERS.md](BLOCKERS.md)、完了判定は [DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md) に記録しています。

## ライセンス

MIT License。詳細は [LICENSE](LICENSE) を参照してください。
