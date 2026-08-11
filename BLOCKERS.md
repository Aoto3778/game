# BLOCKERS.md

## 2026-08-10 — サンドボックス外 Gradle 実行枠

- **何が**: Phase 1 の `:core:test` 144 件成功後に追加実行した `:app:assembleDebug` が、コマンド開始前に環境側から拒否された。
- **なぜ**: Codex のサンドボックス外実行枠が上限に達したという環境メッセージであり、ソースや Gradle の失敗ではない。Phase 0 の APK ビルドは成功済み。
- **代替案 1**: 実行枠が利用可能になった時点で既存 Gradle キャッシュを使い、全検証を再実行する。
- **代替案 2**: GitHub へ push 後、支給された Actions の隔離環境で `:core:test` / `:sim:run` / APK を検証する。
- **代替案 3**: 利用者環境で `./gradlew :core:test :app:assembleDebug` を実行し、そのログを照合する。

Phase 1 の完了条件は拒否前の `:core:test` 144/144 成功で満たした。Android 回帰ビルドは Phase 5 の完成判定まで未確認として保持する。

### Phase 2 への影響

サンドボックス内だけで使える Gradle 配布物と共有 read-only 依存キャッシュも構成したが、環境 ACL が Gradle 自身の JAR close を `AccessDeniedException` で拒否し、Kotlin コンパイル前に停止した。PowerShell による構造監査では、プレイヤーカード 100、敵カード 16、重複 ID 0、敵デッキ参照切れ 0、レリック 40、イベントのカード/レリック参照切れ 0 を確認した。JUnit の実行確認は GitHub Actions または実行枠復旧後まで保留する。

その後、公式 Kotlin 2.1.20 コンパイラ、serialization plugin、JUnit 4.13.2 をワークスペース内だけで実行する検証経路を構成し、`:core` 相当 361 テストと `:sim` 相当 3 Bot × 2,000 ランは成功した。残る blocker は Gradle wrapper 形式の再確認と Android APK の再ビルドだけである。
