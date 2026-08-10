# AGENTS.md

このリポジトリで作業する全てのコーディングエージェントは、以下を常に守ること。

## ビルドと検証（推測禁止・必ず実行する）

```bash
./gradlew :core:test                      # ルールエンジンの検証。最速のループ。まずこれ
./gradlew :sim:run --args="--runs 500"    # バランス統計。数値を変えたら必ず回す
./gradlew :app:assembleDebug              # APK 生成。成果物は app/build/outputs/apk/debug/
./gradlew ktlintCheck                     # 導入していれば
```

`:core:test` が赤い状態で新しい機能を書き始めてはいけない。

## アーキテクチャ不変条件

1. `:core` は Android SDK に依存しない。`import android.*` が現れたら設計ミス
2. ゲーム状態は immutable。更新は `fun reduce(state: GameState, action: Action): GameState` のみ
3. `reduce` は副作用を持たない。乱数は `GameState` 内の seed から決定論的に導出する
   （`kotlin.random.Random` のグローバルインスタンスは使用禁止）
4. 同じ seed + 同じ Action 列 → 必ず同じ最終状態。これを検証するテストを常に緑に保つ
5. `:app` はルールを知らない。`:app` に `if (card.type == ...)` のような分岐が生えたら
   それは `:core` に移すべきロジック

## 禁止事項

- アセットファイル（png/jpg/svg/mp3/wav/ttf）の追加
- ネットワーク通信、`INTERNET` パーミッション
- 広告 / 課金 / アナリティクス / クラッシュレポート SDK
- Google・JetBrains 公式以外のサードパーティ依存
- `!!` の使用（`requireNotNull` + メッセージを使う）
- `@Suppress` によるコンパイラ警告の握りつぶし
- テストを削除・無効化してビルドを通すこと

## コーディング規約

- 1 ファイル 400 行以内。超えたら分割
- public な API には KDoc。カード効果には「なぜこの数値か」をコメントで残す
- マジックナンバーは `Balance.kt` に集約する（`:sim` から一括で振れるようにするため）
- 文言は `strings.xml`（ja / en）に置く。`:core` にユーザー向け文字列を直書きしない

## コミット

- 日本語。`[Phase N] 何をしたか / 何をどう検証したか` の形式
- 1 コミットで複数フェーズをまたがない

## ドキュメント

以下は常に最新に保つこと。

| ファイル | 用途 |
|---|---|
| `PLAN.md` | 残タスクとフェーズ進捗 |
| `DESIGN.md` | ゲーム仕様。実装と食い違ったらこちらを直す |
| `BALANCE.md` | `:sim` の統計出力と、それに基づく数値変更の履歴 |
| `BLOCKERS.md` | 詰まった箇所・理由・代替案 |
| `DEFINITION_OF_DONE.md` | 完成判定チェックリスト |
