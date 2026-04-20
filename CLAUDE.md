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
| Spring Boot | 8080 |
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

## Git フロー

### ブランチ命名
```
feature/#{issue番号}-{機能名}
fix/#{issue番号}-{バグ名}
chore/{作業名}
```

### コミットメッセージ
```
feat: 機能の説明 (#{issue番号})
fix: バグ修正 (#{issue番号})
chore: 設定変更
docs: ドキュメント変更
```

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
