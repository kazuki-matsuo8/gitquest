# データベース・PostgreSQL・Flyway 解説

## データベースとは

データを「永続的に」保存する仕組みです。

「永続的に」が重要です。
アプリを再起動しても、サーバーを止めても、データが消えません。

データベースがなかったら:
- アプリを再起動するたびにユーザーが全員消える
- ミッションの進捗が消える

---

## テーブルとは

データを「表（テーブル）」で管理します。Excel の表をイメージしてください。

```
users テーブル:
| id   | username | email              | password_hash |
|------|----------|--------------------|---------------|
| uuid | kazuki   | kazuki@example.com | $2b$10$...    |
| uuid | tanaka   | tanaka@example.com | $2b$10$...    |
```

行（Row）= 1件のデータ
列（Column）= データの種類（id・username・email...）

---

## SQL の基本

SQL（Structured Query Language）= DB を操作する言語です。

```sql
-- データを取得（SELECT）
SELECT * FROM users WHERE username = 'kazuki';

-- データを追加（INSERT）
INSERT INTO users (username, email, password_hash)
VALUES ('kazuki', 'kazuki@example.com', '$2b$10$...');

-- データを更新（UPDATE）
UPDATE users SET email = 'new@example.com' WHERE id = '...';

-- データを削除（DELETE）
DELETE FROM users WHERE id = '...';
```

これを CRUD（Create / Read / Update / Delete）と呼びます。

---

## GitQuest のテーブル設計

```
users           ← ユーザー情報
missions        ← ミッション定義（「git init しよう」など）
mission_steps   ← ミッションのステップ（サブタスク）
user_progress   ← ユーザーごとの進捗（どこまで終わったか）
```

テーブル同士の関係:

```
users ──── user_progress ──── missions
                                 │
                           mission_steps
```

「1人のユーザーは複数のミッションの進捗を持つ」
「1つのミッションは複数のステップを持つ」

---

## 外部キー（FK）とは

テーブル間の「つながり」を定義するものです。

```sql
CREATE TABLE user_progress (
    user_id    UUID REFERENCES users (id),    -- users テーブルの id を参照
    mission_id UUID REFERENCES missions (id), -- missions テーブルの id を参照
    ...
);
```

`REFERENCES users(id)` = 「存在しないユーザーの進捗は作れない」という制約。
DB レベルでデータの整合性（おかしいデータが入らないこと）を保証します。

---

## UUID とは

主キー（データを一意に識別する ID）に使っています。

```
UUID の例: 550e8400-e29b-41d4-a716-446655440000
連番の例:  1, 2, 3, 4...
```

なぜ UUID を使うのか:
- URL に使っても「何件目か」が推測されない（セキュリティ）
- 例: `/users/1` だと「1番目のユーザー」とわかる → 危険
- 例: `/users/550e8400-...` だと推測できない → 安全

---

## ENUM 型とは

決まった値しか入れられない型です。

```sql
CREATE TYPE progress_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED');
```

`status` カラムには `NOT_STARTED`・`IN_PROGRESS`・`COMPLETED` の3つしか入れられません。
「COMPLETD」（typo）や「done」などの想定外の値を DB レベルで弾けます。

---

## Flyway とは

DB の構造変更を「バージョン管理」するツールです。

なぜ必要か:
```
自分: ローカルの DB に missions テーブルを追加した
→ コードを GitHub に push
→ チームの人が pull したが DB 構造が古いままでエラーになる
```

Flyway を使うと:
- SQL ファイルをバージョン管理する
- アプリ起動時に「まだ実行していない SQL」を自動で実行してくれる
- 全員の DB 構造を常に最新に保てる

```
db/migration/
├── V1__create_users.sql          ← 最初に実行
├── V2__create_missions.sql       ← 次に実行
├── V3__create_user_progress.sql  ← その次
└── V4__insert_missions.sql       ← 最後（初期データ投入）
```

命名規則: `V{番号}__{説明}.sql`
- `V` で始める
- 番号（1, 2, 3...）
- `__`（アンダースコア2つ）
- 説明

起動すると自動実行:
```
INFO: Successfully applied 4 migrations to schema "public"
```

重要: 一度実行した SQL は変更できません。
修正が必要なら新しいバージョンの SQL を追加します（V5__alter_users.sql など）。

---

## INDEX（インデックス）とは

検索を速くする仕組みです。本の「索引」と同じ考え方。

```sql
CREATE INDEX idx_users_email ON users (email);
```

インデックスがない場合:
- `WHERE email = 'kazuki@example.com'` → 全行を1件ずつ確認（100万行あれば100万回）

インデックスがある場合:
- B-Tree 構造で一瞬で見つかる

よく検索条件に使うカラムに設定します。
