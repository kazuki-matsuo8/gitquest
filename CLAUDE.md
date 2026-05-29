# GitQuest — Claude 指示書

## プロジェクト概要
ブラウザ上で Git コマンドを打ちながらコミットグラフがリアルタイムで動く Git 学習アプリ。
Nuxt.js + Spring Boot + PostgreSQL で構成。

---

## ユーザーについて
- プログラミング初心者。Java・JavaScript・SQL の文法もまだ覚えていない段階
- 実装するたびに「なぜこう書くのか」「この技術は何か」を超わかりやすく解説すること
- 解説は `docs/learning/` に Markdown で保存する（git には上げない）
- 用語を使うときは必ず一言説明を添える

---

## 技術スタック

| レイヤー | 技術 | バージョン |
|---------|------|-----------|
| フロント | Nuxt.js | 3.x |
| バック | Spring Boot | 3.4.4 |
| 言語 (バック) | Java | 21 |
| DB | PostgreSQL | 16 |
| ORM | Spring Data JPA + Flyway | - |
| 認証 | Spring Security + JWT | - |
| グラフ描画 | SVG | - |

## ポート番号

| サービス | ポート |
|---------|--------|
| Nuxt.js (dev) | 3000 |
| Spring Boot | 8085 |
| PostgreSQL | 5432 |

---

## フロントエンド実装方針（必須・毎回厳守）

> この方針はフロントエンドの実装時に必ず守ること。例外なし。

- Tailwind CSS を使用してモダンな見た目で実装する
- レスポンシブデザインを必ず実装する（モバイル → タブレット → PC の順で設計）
- TypeScript を使用する
- ダークテーマをベースにする（bg-gray-950 / bg-gray-900 系）
- ホバー・トランジションなどのインタラクションを適切に入れる
- sm: / md: / lg: の Tailwind ブレークポイントを活用する

---

## Issue 駆動開発ルール（必ず守ること）

> **Claude はコードに手を入れる前に必ず issue を確認・作成すること。**
> issue なしで実装を始めることは禁止。

### 作業開始前の手順

```bash
# 1. 未対応の issue を確認
gh issue list

# 2. 該当 issue がなければ作成
gh issue create --title "タイトル" --body "概要" --label "feature"

# 3. ブランチを切る
git checkout -b feature/#{issue番号}-{機能名}
```

### 作業中

```bash
# コミットには必ず issue 番号を含める
git commit -m "feat: 機能の説明 (#{issue番号})"
```

### 作業完了時

```bash
# issue をクローズ（コメント付き）
gh issue close {番号} --comment "実装内容の一言メモ"

# main に push
git push origin {ブランチ名}

# PR を作成（必要に応じて）
gh pr create --title "..." --body "Closes #{issue番号}"
```

### ブランチ命名

```
feature/#{issue番号}-{機能名}   例: feature/#22-user-profile
fix/#{issue番号}-{バグ名}       例: fix/#23-graph-render
chore/{作業名}                  例: chore/update-deps
```

### コミットメッセージ

```
feat: 機能の説明 (#{issue番号})
fix: バグ修正 (#{issue番号})
chore: 設定変更
docs: ドキュメント変更
```

### Milestone の管理

```bash
# 現在の Milestone 一覧
gh api repos/kazuki-matsuo8/gitquest/milestones

# Milestone が完了したらクローズ
gh api --method PATCH repos/kazuki-matsuo8/gitquest/milestones/{番号} -f state=closed
```

### 重要な注意

- **直接 main に push しない** — ブランチ → PR → merge の流れを守る
- **複数機能を 1 issue にまとめない** — 1 issue = 1 目的
- **遡及 issue は OK** — 作業後に issue を作ってすぐクローズしても履歴として残す

---

## よく使うコマンド

### バックエンド
```bash
cd backend
./gradlew bootRun       # 起動
./gradlew compileJava   # コンパイル確認
./gradlew test          # テスト
```

### フロントエンド
```bash
cd frontend
npm run dev             # 起動
npm run typecheck       # 型チェック
```

---

## Spring Boot パッケージ構成

```
com.gitquest.backend
├── config/       # Security・CORS 設定
├── controller/   # REST エンドポイント
├── service/      # ビジネスロジック
├── repository/   # DB アクセス
├── entity/       # JPA エンティティ
├── dto/          # リクエスト・レスポンス
└── security/     # JWT
```

---

## カスタムコマンド（.claude/commands/）

プロジェクト固有のスラッシュコマンドを育てていく。

---

## GitHub

- リポジトリ: https://github.com/kazuki-matsuo8/gitquest
- Milestone: Phase 1〜4
- Issue 駆動で開発を進める
