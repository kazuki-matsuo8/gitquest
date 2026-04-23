# 08 — 擬似ターミナル & SVG コミットグラフ

## この章で学ぶこと

- `ProcessBuilder` を使って Java からシェルコマンドを実行する方法
- セッション管理（UUID + メモリ上の Map）
- SVG の基本構造と Vue での動的描画
- `git log` / `git branch` の出力を解析してグラフデータを作る方法

---

## 1. ProcessBuilder とは

Java で「外部プログラムを起動する」ための仕組みです。  
たとえば `git init` というコマンドを Java のコードから呼び出すことができます。

```java
ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "git init");
pb.directory(workDir.toFile());   // コマンドを実行するディレクトリを指定
pb.redirectErrorStream(true);     // エラー出力を標準出力に合流させる
Process process = pb.start();     // コマンドを起動
```

`Process` オブジェクトから `getInputStream()` で出力を読み取れます。

```java
BufferedReader reader = new BufferedReader(
    new InputStreamReader(process.getInputStream())
);
String output = reader.lines().collect(Collectors.joining("\n"));
process.waitFor(); // コマンドが終了するまで待つ
```

---

## 2. セッション管理

GitQuest では「ユーザーごとに独立した git リポジトリ」を持つ必要があります。  
そのため **セッション** という仕組みを使います。

```
フロントエンド                  バックエンド
  |                               |
  |--- POST /sessions ----------->|  UUID を発行・一時ディレクトリ作成
  |<-- { sessionId: "abc-123" }---|
  |                               |
  |--- POST /sessions/abc-123/exec|  一時ディレクトリで git コマンド実行
  |<-- { output, graph } ---------|
```

セッションデータはメモリ上の `ConcurrentHashMap` で管理します。

```java
// セッション ID → 作業ディレクトリ
private final Map<String, Path> sessions = new ConcurrentHashMap<>();
```

`ConcurrentHashMap` = 複数のリクエストが同時に来ても安全な Map。

一時ディレクトリは `Files.createTempDirectory()` で作ります。

```java
Path workDir = Files.createTempDirectory("gitquest-");
// → /tmp/gitquest-abc12345/ のようなパスが作られる
```

---

## 3. セキュリティ: 許可するコマンドを制限

何でも実行できると危険です。`rm -rf /` などを実行されたら大変。  
そこで許可リスト（allowlist）方式で制限しています。

```java
private static final Set<String> ALLOWED_COMMANDS = Set.of(
    "git init", "git add", "git commit", "git branch",
    "git checkout", "git merge", "git log", "git diff", "git status"
);

private boolean isAllowed(String command) {
    String lower = command.toLowerCase();
    return ALLOWED_COMMANDS.stream().anyMatch(lower::startsWith);
}
```

`startsWith` で「コマンドの先頭が一致するか」を確認しています。  
たとえば `git commit -m "hello"` は `"git commit"` で始まるので許可されます。

---

## 4. git log でコミット情報を取得

`git log --format=...` でコミットの情報を整形して取得できます。

```bash
git log --all --format='%H|%h|%s|%an|%ci|%P'
```

| プレースホルダー | 意味 |
|--------|------|
| `%H` | 完全ハッシュ（40文字） |
| `%h` | 短縮ハッシュ（7文字） |
| `%s` | コミットメッセージの1行目 |
| `%an` | 作者名 |
| `%ci` | 日時 |
| `%P` | 親コミットのハッシュ（スペース区切り） |

出力例:
```
abc1234...|abc1234|first commit|GitQuest User|2026-04-21 10:00:00 +0900|
def5678...|def5678|second commit|GitQuest User|2026-04-21 10:01:00 +0900|abc1234...
```

Java でこれを `|` で分割してオブジェクトに変換しています。

---

## 5. SVG でグラフを描く

SVG (Scalable Vector Graphics) とは、HTML の中に直接書けるベクター画像形式です。

```html
<svg width="400" height="200">
  <!-- 線を引く -->
  <line x1="60" y1="30" x2="60" y2="90" stroke="#3b82f6" stroke-width="2" />

  <!-- 円を描く -->
  <circle cx="60" cy="30" r="10" fill="#22c55e" />

  <!-- テキスト -->
  <text x="80" y="34" fill="white" font-size="12">first commit</text>
</svg>
```

GitQuest では各コミットを円（`<circle>`）で表し、親子関係を線（`<line>`）で繋いでいます。

### Vue での動的 SVG

`v-for` を使って JavaScript の配列から SVG 要素を自動生成しています。

```vue
<svg>
  <circle
    v-for="node in nodes"
    :key="node.hash"
    :cx="node.x"
    :cy="node.y"
    r="10"
    fill="#3b82f6"
  />
</svg>
```

`:cx="node.x"` の `:` は Vue のバインディング記法。JavaScript の値を属性に渡せます。

---

## 6. コンポーネント間のデータの流れ

```
pages/missions/[id].vue
  ├── <TerminalPane>  → コマンドを実行して結果をもらう
  │      emit('graphUpdated', res.graph)
  │
  └── <CommitGraph>   ← graphData を props で受け取って描画
```

Vue では「上から下へ `props` でデータを渡し、下から上へ `emit` でイベントを伝える」のが基本です。

```
親コンポーネント
  │  :graph="graphData"  ← props（データを渡す）
  ↓
子コンポーネント
  │  emit('graphUpdated', data)  ← emit（イベントを上に伝える）
  ↑
親コンポーネント
```

---

## まとめ

| 技術 | 使った場面 |
|------|-----------|
| ProcessBuilder | Java から git コマンドを実行 |
| ConcurrentHashMap | セッション（UUID → 一時ディレクトリ）の管理 |
| 許可リスト | セキュリティ: git 以外のコマンドを弾く |
| git log --format | コミット情報を構造化データとして取得 |
| SVG | ブラウザ上でコミットグラフを描画 |
| Vue emit/props | 親子コンポーネント間のデータ連携 |
