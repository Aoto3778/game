# PLAN.md

## 方針

ゼロサムのドラフト判断を純粋 Kotlin のルールとして先に完成させ、CLI の大量対戦で数値を検証してから Compose UI を接続する。各フェーズは検証コマンドが成功するまで完了扱いにしない。外部アセット、ネットワーク、非公式依存は使わない。

## Phase 0 — 骨格と CI

- [x] `:core` / `:sim` / `:app` の3モジュールと Version Catalog を構築
- [x] JDK 17、minSdk 26、targetSdk 35、難読化なしを設定
- [x] 支給CIと空のCompose画面を配置
- [x] `:core:test :app:assembleDebug` を実測

## Phase 1 — ルールエンジン

- [x] immutable `GameState` と `reduce(state, action)` を実装
- [x] SplitMix64、戦闘、効果順序、状態異常、敵AI、リプレイを実装
- [x] JUnit 80件以上を成功（Phase 4時点で370件）

## Phase 2 — コンテンツ

- [x] 基本カード100種と全アップグレード、敵カード16種、レリック40種を定義
- [x] 敵28種（エリート6、ルール変更ボス4）、分岐イベント25種を定義
- [x] 未実装ID・キーワード参照0件をテスト

## Phase 3 — バランス自動化

- [x] 貪欲型／温存型／シナジー型Botとdenial無効対照を実装
- [x] 3 Bot × 2,000ラン、昇華度0/20、採用率、寄与、詰み率を計測
- [x] 勝率35〜60%、寄与外れ値4枚を達成し `BALANCE.md` に履歴化

## Phase 4 — Compose UI

- [x] タイトル／マップ／戦闘／ドラフト／デッキ／イベント／結果／設定を実装
- [x] 12〜13ノード分岐マップ、Canvasカード、上方向ドラッグと対象領域を実装
- [x] ドロー／破棄、数字ポップ、画面シェイク、80msヒットストップを実装
- [x] coreの3幕通しテストと `:app:assembleDebug` を成功
- [ ] 接続端末での手動通し操作（対象端末なし。`BLOCKERS.md` に代替案を記録）

## Phase 5 — 仕上げと配布

- [x] DataStoreセーブ／レジューム、デイリーseed、統計、実績30個
- [x] AudioTrack合成音、ハプティクス、設定永続化
- [x] 日本語／英語リソース分離、初回ガイド、演出軽減設定
- [x] README、LICENSE、lint/test/build、APK署名／メタデータ検証
- [x] Phase 5コミットを空リポジトリの初期 `main` としてpush
- [x] GitHub Release v1.0.0へ検証済みdebug APKを公開
