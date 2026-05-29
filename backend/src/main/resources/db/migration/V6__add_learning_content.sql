-- learning_content カラムを追加
ALTER TABLE missions ADD COLUMN learning_content TEXT;

-- description を「コマンドを明かさない」表現に更新
UPDATE missions SET description = 'このフォルダを Git で管理できるように初期化してください。初期化すると、Git がファイルの変更履歴を追跡できるようになります。'
WHERE level = 1 AND order_index = 1;

UPDATE missions SET description = '作業フォルダにある README.md をステージングエリアに追加してください。ステージングとは「次のコミットに含めるファイルを選ぶ」作業のことです。'
WHERE level = 1 AND order_index = 2;

UPDATE missions SET description = 'ステージングした変更を記録してください。コミットとは「現時点のスナップショットを保存する」操作です。メッセージには何の変更かを書きます。'
WHERE level = 1 AND order_index = 3;

UPDATE missions SET description = '新しいブランチを作成してください。ブランチとは「作業の分岐点」です。main とは別の流れで開発を進めることができます。'
WHERE level = 2 AND order_index = 1;

UPDATE missions SET description = '作成した feature ブランチに切り替えてください。切り替えることで、そのブランチ上での作業が始まります。'
WHERE level = 2 AND order_index = 2;

UPDATE missions SET description = 'feature ブランチの変更を main ブランチに取り込んでください。これにより、別のブランチで行った作業を main に統合できます。'
WHERE level = 2 AND order_index = 3;

UPDATE missions SET description = 'これまでのコミット履歴を確認してください。誰がいつどんな変更をしたかを一覧で見ることができます。'
WHERE level = 3 AND order_index = 1;

UPDATE missions SET description = 'ファイルの変更内容を確認してください。コミット前に「何を変えたのか」を把握することができます。'
WHERE level = 3 AND order_index = 2;

UPDATE missions SET description = 'ワーキングディレクトリとステージングエリアの現在の状態を確認してください。変更済み・ステージ済み・未追跡のファイルが一目でわかります。'
WHERE level = 3 AND order_index = 3;

-- 各ミッションに learning_content を設定
UPDATE missions SET learning_content = '# Git とは？

Git は **バージョン管理システム** です。ファイルの変更履歴をすべて記録しておき、「どこをいつ誰が変えたか」を追跡できます。

## なぜ必要なの？

コードを書いていると、こんな場面がよくあります。

- 「前の状態に戻したい」
- 「Aさんの変更と Bさんの変更を合わせたい」
- 「どのバージョンで動いていたか確認したい」

Git はこういった悩みをすべて解決してくれます。

## git init とは？

`git init` は「このフォルダを Git で管理しはじめる」コマンドです。実行すると `.git` という隠しフォルダが作られ、Git がそこに履歴を保存します。

```
プロジェクトフォルダ/
├── .git/          ← Git の管理データが入る
├── README.md
└── ...
```

一度だけ実行すれば OK です。'
WHERE level = 1 AND order_index = 1;

UPDATE missions SET learning_content = '# ステージングとは？

Git では変更を記録する前に「ステージング」という工程があります。

## 3 つのエリア

```
ワーキングディレクトリ  →  ステージング  →  リポジトリ
 (ファイルを編集)        (git add)       (git commit)
```

| エリア | 説明 |
|--------|------|
| ワーキングディレクトリ | 実際に作業するフォルダ |
| ステージング | コミットに含めるファイルを選ぶ場所 |
| リポジトリ | 確定した履歴が保存される場所 |

## なぜステージングが必要？

一度に変更したファイルが 10 個あっても、「このファイルだけコミットしたい」という場面があります。ステージングはその取捨選択をする場所です。

## git add の使い方

```bash
git add ファイル名     # 特定のファイルだけ追加
git add .             # すべての変更を追加
```'
WHERE level = 1 AND order_index = 2;

UPDATE missions SET learning_content = '# コミットとは？

コミットは「今の状態を保存する」操作です。ゲームのセーブポイントのようなイメージです。

## コミットが作るもの

```
コミット A ← コミット B ← コミット C (最新)
```

コミットは連なっていて、前のコミットへの参照を持っています。これが「履歴」になります。

## 良いコミットメッセージとは？

```bash
git commit -m "ログイン機能を追加"      # OK: 何をしたか明確
git commit -m "修正"                    # NG: 何を修正したのか不明
git commit -m "2024/04/01 の変更"       # NG: いつかはわかるが何かがわからない
```

## コミットのベストプラクティス

- 小さな単位でこまめにコミットする
- メッセージは「何をしたか」を動詞ではじめる
- 1 コミット = 1 つの目的'
WHERE level = 1 AND order_index = 3;

UPDATE missions SET learning_content = '# ブランチとは？

ブランチは「作業の分岐点」です。main ブランチとは別の流れで開発を進めることができます。

## ブランチを使うと何が嬉しい？

```
main     ●─────────────────────────●
              ↘                  ↗
feature        ●─────●─────●
```

たとえば「新機能の開発」と「バグ修正」を同時に、互いに影響させずに進められます。

## よくあるブランチの使い方

| ブランチ名 | 用途 |
|-----------|------|
| main / master | リリース済みの安定版 |
| feature/xxx | 新機能の開発 |
| fix/xxx | バグ修正 |

## git branch コマンド

```bash
git branch              # 現在のブランチ一覧を表示
git branch feature      # feature という名前のブランチを作成
```

ブランチを作っただけでは、まだそのブランチに移動していないことに注意してください。'
WHERE level = 2 AND order_index = 1;

UPDATE missions SET learning_content = '# ブランチの切り替え

ブランチを作っただけでは、そのブランチで作業できません。「切り替え（チェックアウト）」が必要です。

## HEAD とは？

Git では今自分がいるブランチを **HEAD** と呼びます。

```
main     ●───●───● ← HEAD がここにいると main での作業
              ↘
feature        ●   ← checkout で HEAD をここに移す
```

## git checkout コマンド

```bash
git checkout feature      # feature ブランチに切り替え
git checkout main         # main ブランチに戻る
```

## 最近の書き方

新しい Git では `git switch` も使えます。

```bash
git switch feature        # checkout と同じ動き
git switch -c new-branch  # 作成 + 切り替えを同時に
```

切り替えた後は `git branch` で現在のブランチ（* がついている方）を確認しましょう。'
WHERE level = 2 AND order_index = 2;

UPDATE missions SET learning_content = '# マージとは？

マージは「別のブランチの変更を取り込む」操作です。

## マージの流れ

```
main     ●───●─────────────●  ← マージコミット
              ↘            ↗
feature        ●───●───●
```

feature ブランチで行った変更が main に統合されます。

## 2 種類のマージ

**Fast-forward マージ**
main が feature の分岐点と同じ場合、単純に先へ進むだけです（マージコミットが作られない）。

**マージコミット**
main と feature が別々に進んでいた場合、2 つの履歴を統合するコミットが作られます。

## マージの手順

```bash
git checkout main         # 取り込む先のブランチに移動
git merge feature         # feature の変更を取り込む
```

マージ後に feature ブランチは削除してもかまいません。'
WHERE level = 2 AND order_index = 3;

UPDATE missions SET learning_content = '# コミット履歴の確認

`git log` は「誰がいつ何をしたか」の履歴を一覧表示するコマンドです。

## 基本の使い方

```bash
git log
```

出力される情報：
- **commit** ハッシュ値（コミットの識別子）
- **Author** 変更した人
- **Date** 変更した日時
- メッセージ

## 便利なオプション

```bash
git log --oneline         # 1 行で簡潔に表示
git log --graph           # ブランチのグラフ付きで表示
git log --all             # すべてのブランチの履歴を表示
```

## コミットハッシュとは？

各コミットには `a3f2c1d...` のような一意の ID があります。これを使って特定のコミットに戻ったり、比較したりできます。'
WHERE level = 3 AND order_index = 1;

UPDATE missions SET learning_content = '# 変更内容の確認

`git diff` は「何が変わったか」を表示するコマンドです。コミットする前に自分の変更を確認する習慣をつけましょう。

## 基本の使い方

```bash
git diff
```

`-` で始まる行が削除された箇所、`+` で始まる行が追加された箇所です。

```diff
- 古い行
+ 新しい行
```

## よく使うオプション

```bash
git diff                      # ステージ前の変更を確認
git diff --staged             # ステージ済みの変更を確認
git diff HEAD                 # 最新コミットとの差分
git diff コミットA コミットB  # 2 つのコミットを比較
```

## diff を読む習慣

コミット前に必ず `git diff` で変更を確認することで、意図しない変更の混入を防げます。'
WHERE level = 3 AND order_index = 2;

UPDATE missions SET learning_content = '# 現在の状態を確認する

`git status` は「今 Git がどんな状態にあるか」を表示するコマンドです。最もよく使うコマンドの一つです。

## 表示される情報

```
On branch main

Changes to be committed:        ← ステージ済み（コミット待ち）
  modified: README.md

Changes not staged for commit:  ← 変更されたがステージ未
  modified: app.js

Untracked files:                ← Git が管理していないファイル
  new-file.txt
```

## 3 つの状態

| 状態 | 意味 |
|------|------|
| Untracked | Git が知らないファイル |
| Modified | 変更されたが未ステージ |
| Staged | ステージ済み・コミット待ち |

## よく使うコマンドの流れ

```bash
git status          # 状態確認
git add .           # ステージ
git status          # 再確認
git commit -m "..."  # コミット
```'
WHERE level = 3 AND order_index = 3;
