# JWT 認証と Spring Security 解説

## 認証とは何か

アプリを使うとき「あなたは誰ですか？」を確認する仕組みです。

認証がないと:
- 誰でも誰の進捗でも見られる
- 誰でも誰の進捗を消せる

ログインした人だけが自分のデータにアクセスできるように守ります。

---

## セッション認証 vs JWT 認証

### 昔ながらのセッション認証

```
1. ユーザーがログイン
2. サーバーが「セッション情報」をサーバー内のメモリに保存
3. ブラウザにセッション ID をクッキーで渡す
4. 次のリクエストでクッキーを送る → サーバーで照合
```

問題: サーバーにデータを保存するので、サーバーが複数台になると厄介。

### JWT 認証（今回使う方法）

```
1. ユーザーがログイン
2. サーバーが JWT トークンを生成してクライアントに渡す
3. クライアント（ブラウザ）がトークンを保存
4. 次のリクエストでトークンを Authorization ヘッダーにつける
5. サーバーはトークンを「検証」するだけ（何も保存しない）
```

メリット: サーバーに状態を保存しない（Stateless）

---

## JWT の構造

JWT（JSON Web Token）は3つのパーツが `.` でつながっています。

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrYXp1a2lAZXhhbXBsZS5jb20ifQ.abc123
^---ヘッダー---^      ^--------ペイロード--------^              ^署名^
```

Base64 デコードすると中身が見えます:

ペイロード（本体）:
```json
{
  "sub": "kazuki@example.com",  // 誰のトークンか
  "iat": 1776402935,             // 発行時刻
  "exp": 1776489335              // 有効期限
}
```

署名: サーバーだけが知る秘密鍵で作ったハッシュ値。
改ざんされると署名が合わなくなるのでバレます。

重要: JWT の中身は誰でも見られます（暗号化ではない）。
パスワードなど秘密情報は絶対に入れてはいけません。

---

## 実装ファイルの役割

### JwtUtil.java — トークンの生成と検証

```java
// トークンを生成する
public String generateToken(String email) {
    return Jwts.builder()
            .subject(email)       // 誰のトークンか
            .issuedAt(new Date()) // 発行時刻
            .expiration(...)      // 有効期限（24時間後）
            .signWith(secretKey)  // 秘密鍵で署名
            .compact();           // 文字列に変換
}
```

### JwtAuthenticationFilter.java — 全リクエストをチェック

全てのリクエストに割り込んで JWT を検証します。

```
リクエスト受信
    ↓
Authorization ヘッダーを確認
    ↓ "Bearer eyJhbG..." の形式かチェック
トークンを取り出して検証
    ↓ 有効なら
Spring Security のコンテキストに「誰からのリクエストか」をセット
    ↓
Controller へ（通常の処理）
```

### SecurityConfig.java — どのURLを守るか設定

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()  // ログイン・登録は誰でもOK
    .anyRequest().authenticated()                  // それ以外はログイン必須
)
```

`/api/auth/**` を認証不要にしないと、そもそもログインできなくなってしまいます。

### AuthService.java — ログイン・登録のロジック

```java
// 登録処理の流れ
public AuthResponse register(RegisterRequest request) {
    // 1. メールアドレスの重複チェック
    if (userRepository.existsByEmail(request.email())) {
        throw new IllegalArgumentException("すでに使用されています");
    }
    // 2. パスワードをハッシュ化して保存
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    // 3. JWT トークンを生成して返す
    String token = jwtUtil.generateToken(saved.getEmail());
    return AuthResponse.of(token, ...);
}
```

---

## bcrypt とは

パスワードを安全に保存するためのハッシュ関数です。

```
パスワード: "password123"
      ↓ bcrypt でハッシュ化
保存値: "$2b$10$EixZaYVK1fsbw1ZfbX3OXe..."
```

「ハッシュ化」= 元に戻せない変換。
DB が流出してもハッシュから元のパスワードはわかりません。

ログイン時の照合:
```java
// ハッシュ化して比較するので元のパスワードは不要
passwordEncoder.matches("password123", storedHash) // true or false
```

---

## record とは（Java 16+）

データを運ぶだけのクラスを短く書ける構文です。

```java
// 通常のクラス（長い）
public class LoginRequest {
    private final String email;
    private final String password;
    public LoginRequest(String email, String password) { ... }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

// record（シンプル）
public record LoginRequest(String email, String password) {}
// コンストラクタ・getter が自動生成される
```

DTO（データを運ぶだけのクラス）に最適な書き方です。

---

## CORS とは

「別のドメインからのリクエストをブロックする」ブラウザの機能です。

```
フロントエンド: http://localhost:3000  (Nuxt.js)
バックエンド:   http://localhost:8080  (Spring Boot)
```

ポート番号が違うので「別オリジン」 = CORS の制限が発生します。
Spring Boot 側で「localhost:3000 は OK」と設定することで解決します。

---

## @アノテーション とは

Java のアノテーション = クラスやメソッドへの「ラベル」です。

```java
@Entity           // このクラスはDBのテーブルと対応している
@Table(name = "users")  // テーブル名は "users"
public class User {

    @Id           // これが主キー
    @Column(nullable = false)  // NOT NULL 制約
    private String email;
}
```

`@アノテーション` を書くだけで Spring Boot が自動で設定してくれます。
自分でゼロから書く必要がなく、宣言するだけで動くのが Spring Boot の強みです。

---

## API の確認方法

```bash
# ユーザー登録
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"kazuki","email":"kazuki@example.com","password":"password123"}'

# ログイン → accessToken が返ってくる
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"kazuki@example.com","password":"password123"}'

# 認証が必要な API（トークンをつける）
curl http://localhost:8080/api/missions \
  -H "Authorization: Bearer eyJhbGci..."
```

`curl` = ターミナルから HTTP リクエストを送るコマンド。
`-H` = ヘッダーを指定。`-d` = 送るデータ（JSON）。
