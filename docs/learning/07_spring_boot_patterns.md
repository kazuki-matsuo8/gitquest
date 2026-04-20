# Spring Boot パターン解説

## レイヤードアーキテクチャ

Spring Boot は役割ごとにクラスを分けます。

```
HTTP リクエスト
    ↓
Controller  ← リクエストを受け取るだけ
    ↓
Service     ← ビジネスロジック（何をするか）
    ↓
Repository  ← DB アクセスだけ
    ↓
Database
```

なぜ分けるのか:
- Controller を変えても Service・Repository は変えなくていい
- Service だけテストできる
- 「どこに何を書くか」が明確になる

---

## Controller パターン

```java
@RestController               // このクラスは REST API を提供する
@RequestMapping("/api/missions") // このクラスの URL プレフィックス
public class MissionController {

    // コンストラクタインジェクション（後述）
    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    // GET /api/missions
    @GetMapping
    public ResponseEntity<List<MissionResponse>> getAll() {
        return ResponseEntity.ok(missionService.getAll());
    }

    // GET /api/missions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MissionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(missionService.getById(id));
    }

    // POST /api/missions
    @PostMapping
    public ResponseEntity<MissionResponse> create(
            @Valid @RequestBody CreateMissionRequest request) {
        // @Valid = バリデーション実行
        // @RequestBody = リクエストボディ（JSON）を Java オブジェクトに変換
        return ResponseEntity.ok(missionService.create(request));
    }
}
```

`ResponseEntity<T>` = HTTP ステータスコードとボディをまとめたクラス。
`ResponseEntity.ok(data)` = ステータス 200 + data を返す。

---

## Service パターン

```java
@Service  // Spring が「これはサービスクラス」と認識する
public class MissionService {

    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    // readOnly = true をつけると DB への書き込みを禁止（パフォーマンス向上）
    @Transactional(readOnly = true)
    public List<MissionResponse> getAll() {
        return missionRepository.findAll()
                .stream()
                .map(MissionResponse::from) // Mission → MissionResponse に変換
                .toList();
    }

    @Transactional  // DB への書き込みを含む処理
    public MissionResponse create(CreateMissionRequest request) {
        Mission mission = new Mission();
        mission.setTitle(request.title());
        // ...
        Mission saved = missionRepository.save(mission);
        return MissionResponse.from(saved);
    }
}
```

`@Transactional` = 処理の途中でエラーになっても DB を元に戻してくれる（ロールバック）。

---

## Repository パターン

```java
// JpaRepository<エンティティの型, 主キーの型> を extends するだけ
public interface UserRepository extends JpaRepository<User, UUID> {

    // メソッド名のルールに従うだけで SQL が自動生成される
    Optional<User> findByEmail(String email);
    // → SELECT * FROM users WHERE email = ?

    List<User> findByUsernameContaining(String keyword);
    // → SELECT * FROM users WHERE username LIKE '%keyword%'

    boolean existsByEmail(String email);
    // → SELECT COUNT(*) > 0 FROM users WHERE email = ?
}
```

Spring Data JPA のメソッド名ルール:

| メソッド名 | 意味 |
|-----------|------|
| `findBy〇〇` | WHERE 〇〇 = ? |
| `findBy〇〇And△△` | WHERE 〇〇 = ? AND △△ = ? |
| `findBy〇〇OrderBy△△Asc` | ORDER BY △△ ASC |
| `existsBy〇〇` | 存在確認（boolean） |
| `countBy〇〇` | 件数 |
| `deleteBy〇〇` | 削除 |

---

## Entity パターン

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // UUID を自動生成
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // テーブルのカラム名と Java のフィールド名が違う場合
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // 他のテーブルへの参照（多対一）
    @ManyToOne(fetch = FetchType.LAZY) // LAZY = 必要になるまで DB から取得しない
    @JoinColumn(name = "mission_id")   // 外部キーのカラム名
    private Mission mission;

    // 一対多（1つのミッションに複数のステップ）
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    private List<MissionStep> steps = new ArrayList<>();

    // INSERT 前に自動実行
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    // UPDATE 前に自動実行
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
```

---

## 依存性注入（DI）

Spring Boot の重要な概念です。

「依存する」= あるクラスが別のクラスを使うこと。

```java
// DI なし（悪い例）
public class MissionService {
    // 自分で new している → テストしにくい、差し替えにくい
    private MissionRepository repository = new MissionRepository();
}

// DI あり（良い例）
@Service
public class MissionService {
    private final MissionRepository repository;

    // コンストラクタで受け取る → Spring が自動で渡してくれる
    public MissionService(MissionRepository repository) {
        this.repository = repository;
    }
}
```

`@Service`・`@Repository`・`@Controller`・`@Component` をつけると
Spring が自動でオブジェクトを作って必要なところに渡してくれます。
これを「依存性注入（Dependency Injection / DI）」と呼びます。

---

## DTO パターン

DTO（Data Transfer Object）= データを運ぶだけのクラス。

なぜ Entity をそのまま返さないのか:
- `password_hash` など返してはいけないフィールドが漏れる
- API のレスポンス形式と Entity の形式が異なることが多い

```java
// Entity（DB の形）
public class User {
    private UUID id;
    private String username;
    private String email;
    private String passwordHash; // ← 外部に返してはいけない
}

// DTO（API のレスポンスの形）
public record UserResponse(UUID id, String username, String email) {
    // Entity → DTO に変換するファクトリメソッド
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
        // passwordHash は含めない
    }
}
```

---

## バリデーション

リクエストの入力値チェックを自動化します。

```java
// リクエスト DTO にアノテーションをつける
public record RegisterRequest(
        @NotBlank                    // 空文字・null を拒否
        @Size(min = 3, max = 50)     // 3〜50文字
        String username,

        @NotBlank
        @Email                       // メール形式チェック
        String email,

        @NotBlank
        @Size(min = 8)               // 8文字以上
        String password
) {}
```

```java
// Controller で @Valid をつけるだけで自動チェック
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request) {
    // バリデーション失敗なら自動で 400 エラーを返す
    return ResponseEntity.ok(authService.register(request));
}
```

主なバリデーションアノテーション:

| アノテーション | 意味 |
|-------------|------|
| `@NotBlank` | null・空文字・空白のみを拒否 |
| `@NotNull` | null を拒否 |
| `@Email` | メール形式チェック |
| `@Size(min, max)` | 文字数チェック |
| `@Min(value)` | 最小値チェック（数値） |
| `@Max(value)` | 最大値チェック（数値） |

---

## 例外ハンドリング

`@ControllerAdvice` でアプリ全体の例外を一元管理します。

```java
@RestControllerAdvice  // 全 Controller の例外をここでキャッチ
public class GlobalExceptionHandler {

    // IllegalArgumentException が起きたら
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }

    // BadCredentialsException が起きたら
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(401)
                .body(new ErrorResponse(e.getMessage()));
    }

    public record ErrorResponse(String message) {}
}
```

各 Controller に try-catch を書かなくていいのでコードがスッキリします。
