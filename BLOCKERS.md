# BLOCKERS.md

## 解消済み — Windows 非 ASCII パスでの Gradle 実行

- **事象**: OneDrive の日本語パスを直接使うと、Gradle のテストワーカーがテストクラスを読み込めなかった。
- **原因**: Android のパス検査と Windows の子プロセスクラスパス文字化けが重なっていた。
- **解決**: `android.overridePathCheck=true` を設定し、ASCII 名の一時ジャンクション経由で Gradle を実行した。
- **検証**: `:core:test :app:assembleDebug` 成功、core 370件成功、debug APK 11,828,319 bytes。

## 2026-08-11 — Phase 4 実機／エミュレータ通し操作

- **何が**: 接続端末または起動済みエミュレータでの手動1ラン通し操作と、プロセス強制終了後の復帰操作だけを実施できない。
- **なぜ**: `adb devices -l` の結果が空で、この環境に操作対象がないため。コード、APK、core 通しテストの失敗ではない。
- **代替案 1**: Android 26以上の実機をUSBデバッグ接続し、debug APKをインストールして3幕を操作する。
- **代替案 2**: Android StudioでAPI 35の縦持ちAVDを作成し、同じAPKをインストールして操作する。
- **代替案 3**: GitHub ActionsでAPKを生成し、端末のある検証者がチェックリストに沿って通し操作する。
- **継続判断**: ルール上の全経路は `RunFlowIntegrationTest`、UIのコンパイル／パッケージは `assembleDebug` で検証済み。作業規律に従い、この項目だけを保留して Phase 5 を進める。
