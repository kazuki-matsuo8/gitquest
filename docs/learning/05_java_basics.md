# Java 基本文法

## Java とは

Spring Boot のバックエンドで使う言語です。
「型」を必ず書く、静的型付け言語です。

---

## 変数と型

```java
// 型名 変数名 = 値;
int age = 21;           // 整数
double price = 9.99;    // 小数
boolean isLoggedIn = true; // true/false
String name = "kazuki"; // 文字列（String は大文字始まり）

// var を使うと型を自動推論（Java 10+）
var count = 0;          // int と推論される
var message = "hello";  // String と推論される
```

JavaScript との違い:
```js
// JavaScript: let / const だけでいい
let age = 21;
const name = "kazuki";
```

```java
// Java: 型を明示する必要がある
int age = 21;
final String name = "kazuki"; // final = 変更不可（JS の const 相当）
```

---

## メソッド（関数）

```java
// 戻り値の型 メソッド名(引数の型 引数名) { ... }
public int add(int a, int b) {
    return a + b;
}

// 戻り値がない場合は void
public void printHello(String name) {
    System.out.println("Hello, " + name);
}
```

JavaScript との違い:
```js
// JavaScript
function add(a, b) { return a + b; }
const add = (a, b) => a + b; // アロー関数
```

```java
// Java: 型の宣言が必須
public int add(int a, int b) { return a + b; }
// アロー関数はない（ラムダ式という似たものはある）
```

---

## クラス

Java は全てのコードをクラスの中に書きます。

```java
public class User {
    // フィールド（クラスが持つ変数）
    private String username;
    private String email;

    // コンストラクタ（new するときに呼ばれる）
    public User(String username, String email) {
        this.username = username; // this = このオブジェクト自身
        this.email = email;
    }

    // メソッド（クラスが持つ関数）
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

// 使い方
User user = new User("kazuki", "kazuki@example.com");
System.out.println(user.getUsername()); // "kazuki"
```

`private` = クラスの外からアクセスできない
`public` = どこからでもアクセスできる

---

## アクセス修飾子

| 修飾子 | 意味 |
|--------|------|
| `public` | どこからでもアクセス可 |
| `private` | クラス内だけ |
| `protected` | 同じパッケージ + サブクラス |
| なし | 同じパッケージのみ |

フィールドは `private`、メソッドは `public` にするのが基本です（カプセル化）。

---

## getter / setter

フィールドを `private` にすると外から直接触れないので、
getter（取得）と setter（変更）メソッドを作ります。

```java
private String username;

// getter: get + フィールド名（大文字始まり）
public String getUsername() {
    return username;
}

// setter: set + フィールド名（大文字始まり）
public void setUsername(String username) {
    this.username = username;
}
```

---

## コレクション（複数データの管理）

```java
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

// List = 順番のある配列
List<String> names = new ArrayList<>();
names.add("kazuki");
names.add("tanaka");
System.out.println(names.get(0)); // "kazuki"

// Map = キーと値のペア（JavaScript のオブジェクトに近い）
Map<String, Integer> scores = new HashMap<>();
scores.put("kazuki", 100);
scores.put("tanaka", 85);
System.out.println(scores.get("kazuki")); // 100
```

`<String>` の部分をジェネリクスといいます。「何型のデータを入れるか」を指定します。

---

## Stream API

コレクションを変換・絞り込む便利な記法です。

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

// filter: 条件に合うものだけ残す（JavaScript の .filter と同じ）
List<Integer> evens = numbers.stream()
        .filter(n -> n % 2 == 0)
        .toList();
// → [2, 4]

// map: 各要素を変換する（JavaScript の .map と同じ）
List<String> strings = numbers.stream()
        .map(n -> "No." + n)
        .toList();
// → ["No.1", "No.2", ...]

// collect: グルーピング
Map<Integer, List<Mission>> grouped = missions.stream()
        .collect(Collectors.groupingBy(Mission::getLevel));
```

`n -> n % 2 == 0` はラムダ式（JavaScript のアロー関数に近い）です。

---

## Optional

値が `null` かもしれないときに使うラッパーです。

```java
// null チェックの地獄を防ぐ
Optional<User> userOpt = userRepository.findByEmail("kazuki@example.com");

// 値があれば取得、なければ例外
User user = userOpt.orElseThrow(() -> new RuntimeException("見つかりません"));

// 値があれば処理、なければスキップ
userOpt.ifPresent(u -> System.out.println(u.getUsername()));

// map で変換
Optional<String> username = userOpt.map(User::getUsername);
```

Spring Boot の Repository は `Optional<T>` を返すことが多いです。

---

## interface（インターフェース）

「このメソッドを必ず実装してください」という約束事です。

```java
// interface: 実装は書かない、宣言だけ
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email); // 宣言だけ
}

// Spring Data JPA が自動で実装を作ってくれる（自分で書かなくていい）
```

---

## record（Java 16+）

データを持つだけのクラスを短く書ける構文です。

```java
// 通常のクラス（長い）
public class LoginRequest {
    private final String email;
    private final String password;
    public LoginRequest(String email, String password) {
        this.email = email; this.password = password;
    }
    public String email() { return email; }
    public String password() { return password; }
}

// record（シンプル）
public record LoginRequest(String email, String password) {}
// 上と全く同じ動きをする
```

DTO（データを運ぶだけのクラス）に使います。

---

## アノテーション

クラス・メソッドへの「ラベル」です。Spring Boot が読んで自動設定します。

```java
@Entity           // このクラスは DB テーブルと対応している
@Table(name = "users")
public class User {

    @Id           // 主キー
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;
}

@RestController   // HTTP リクエストを受け取るクラス
@RequestMapping("/api/missions")
public class MissionController {

    @GetMapping("/{id}")  // GET /api/missions/{id} にマッピング
    public ResponseEntity<MissionResponse> getById(@PathVariable UUID id) {
        // ...
    }
}

@Service          // ビジネスロジックのクラス
public class MissionService { }

@Repository       // DB アクセスのクラス（JpaRepository を使う場合は不要）
public interface UserRepository extends JpaRepository<User, UUID> { }
```

---

## 例外処理

```java
// 例外を投げる
throw new IllegalArgumentException("ミッションが見つかりません");

// 例外を受け取る
try {
    User user = findUser(id); // 例外が発生するかもしれない処理
} catch (IllegalArgumentException e) {
    System.out.println("エラー: " + e.getMessage());
} finally {
    // 例外の有無にかかわらず必ず実行
}
```

Spring Boot では `@ControllerAdvice` で例外を一元管理します（各 Controller でバラバラに書かない）。
