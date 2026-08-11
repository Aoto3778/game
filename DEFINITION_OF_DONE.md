# DEFINITION OF DONE

チェックはコマンド出力または実機操作を根拠に付ける。未実施項目を自己申告で完了扱いにしない。

## ビルドと分離

- [x] `:core:test` が成功し、テスト数150件以上（378件、失敗0）
- [x] `:app:assembleDebug` が成功
- [ ] GitHub Actions の全ジョブが成功し、APKがArtifactsに存在
- [x] `:core` / `:sim` の Android／AndroidX import が0件
- [x] Kotlinソースの `!!` が0件
- [x] AndroidManifest に `INTERNET` 権限がない

## 決定論とコンテンツ

- [x] 同じseedとAction列が同じ最終状態になるテストがある
- [x] `Random.Default` / `Math.random` の使用が0件
- [x] 基本カード100種と全アップグレード
- [x] レリック40種
- [x] 敵28種（エリート6、ボス4）
- [x] 分岐イベント25種
- [x] 実績30個
- [x] 未定義ID／キーワード参照0件を検証するテストがある

## バランス

- [x] 3 Bot × 2,000ランの勝率が35〜60%
- [x] 昇華度0と20の勝率差が20pt以上
- [x] 採用率0%が0枚、90%超が3枚以下
- [x] `BALANCE.md` に4回の調整サイクルと最終値がある
- [x] denial有効／無効の比較で勝率差を数値化

## プレイ体験

- [x] reducerの通しテストで3幕と最終ボスを完走
- [ ] 実機またはエミュレータで3幕クリアまで手動通しプレイ（接続端末なし。`BLOCKERS.md`参照）
- [x] 戦闘途中GameStateのDataStore保存／起動時復元経路とJSON往復をテスト
- [ ] 実機で強制終了後に同じ戦闘途中へ復帰（接続端末なし。`BLOCKERS.md`参照）
- [x] ドラフト全カードに敵の使い方予告を表示
- [x] 敵の次行動を常時表示
- [x] 主操作を縦持ち画面下2/3に配置
- [x] 初回戦闘に3段階ガイドを表示
- [x] 演出軽減設定が画面シェイク回数に反映
- [x] 戻る操作はcoreの画面遷移を通り、意図しないラン消失がない

## 仕上げと配布

- [x] 戦闘途中セーブ、日付seedのデイリー、永続統計、実績30個
- [x] AudioTrack合成音とハプティクス
- [x] ダークテーマ固定、英語／日本語の文字列リソース分離
- [x] `README.md` にAPKビルド／インストール手順とASCII UI図がある
- [x] MIT Licenseがある
- [x] lint 0件、APK v2 debug署名、package/minSdk/targetSdk metadataを最終検証
- [ ] Phase 5コミットをGitHubへpushし、配布APKを公開
