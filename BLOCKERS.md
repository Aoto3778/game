# BLOCKERS.md

## 2026-08-10 — サンドボックス外 Gradle 実行枠

- **何が**: Phase 1 の `:core:test` 144 件成功後に追加実行した `:app:assembleDebug` が、コマンド開始前に環境側から拒否された。
- **なぜ**: Codex のサンドボックス外実行枠が上限に達したという環境メッセージであり、ソースや Gradle の失敗ではない。Phase 0 の APK ビルドは成功済み。
- **代替案 1**: 実行枠が利用可能になった時点で既存 Gradle キャッシュを使い、全検証を再実行する。
- **代替案 2**: GitHub へ push 後、支給された Actions の隔離環境で `:core:test` / `:sim:run` / APK を検証する。
- **代替案 3**: 利用者環境で `./gradlew :core:test :app:assembleDebug` を実行し、そのログを照合する。

Phase 1 の完了条件は拒否前の `:core:test` 144/144 成功で満たした。Android 回帰ビルドは Phase 5 の完成判定まで未確認として保持する。
