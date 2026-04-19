# Spring Boot セットアップ解説

## Spring Boot とは

Java でウェブアプリのバックエンド（サーバー側）を作るフレームワークです。

「フレームワーク」= アプリに必要な土台がまとめて用意されたツールセット。
自分でゼロから書かなくていい部分（HTTP 通信・DB 接続など）を肩代わりしてくれます。

---

## バックエンドって何をするの？

```
ユーザー（ブラウザ）
    │  「ミッション一覧ください」
    ↓
フロントエンド (Nuxt.js)   ← 画面を作る・見た目
    │  「/api/missions にリクエスト」
    ↓
バックエンド (Spring Boot) ← ロジック処理・データ管理  ← 今ここ
    │  「SELECT * FROM missions」
    ↓
データベース (PostgreSQL)  ← データの保存場所
```

---

## Gradle とは

Java のビルドツール（まとめ役）です。

やってくれること:
- 外部ライブラリを自動でダウンロード
- Java コードをコンパイル（人間が読める → コンピュータが読める）
- テストを実行
- アプリを起動

```bash
./gradlew bootRun       # アプリ起動
./gradlew compileJava   # コンパイルだけ
./gradlew test          # テスト実行
```

`./gradlew` の `./` は「このフォルダにある gradlew を使う」という意味。
PC に Gradle をインストールしなくても動くのが Gradle Wrapper の便利なところです。

---

## build.gradle の読み方

```groovy
plugins {
    id 'org.springframework.boot' version '3.4.4'  // Spring Boot を使う
}

dependencies {
    // implementation = 本番コードで使うライブラリ
    implementation 'org.springframework.boot:spring-boot-starter-web'      // HTTP サーバー機能
    implementation 'org.springframework.boot:spring-boot-starter-security' // 認証・認可
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa' // DB 操作
    runtimeOnly 'org.postgresql:postgresql'                                 // PostgreSQL ドライバ

    // testImplementation = テストでだけ使うライブラリ
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

`spring-boot-starter-web` を追加するだけで、HTTP サーバー（Tomcat）が内蔵されます。
設定ゼロで `./gradlew bootRun` するだけで 8080 番ポートでサーバーが立ち上がります。

---

## application.yml の仕組み

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/gitquest
    username: ${DB_USERNAME:matsuo}   # 環境変数 DB_USERNAME があればそれ、なければ matsuo
    password: ${DB_PASSWORD:matsuo}
```

`${変数名:デフォルト値}` という書き方が Spring Boot の環境変数注入です。

なぜ直接書かないの？
→ パスワードをコードに直書きすると、GitHub に push したとき漏洩します。
→ 本番環境では環境変数として設定し、コードには含めません。

---

## パッケージ構成と役割

```
com.gitquest.backend
├── config/       ← Security・CORS の設定
├── controller/   ← HTTP リクエストを受け取る入り口
├── service/      ← アプリのロジック（何をするか）
├── repository/   ← DB へのアクセス
├── entity/       ← DB のテーブルに対応する Java クラス
├── dto/          ← API のリクエスト・レスポンスの形
└── security/     ← JWT の発行・検証
```

なぜ分けるの？
→「1つのクラスは1つの仕事だけ」という原則（単一責任の原則）があるためです。
→ 分けることで修正・テストがしやすくなります。
