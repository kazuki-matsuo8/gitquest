# Nuxt.js セットアップ解説

## Nuxt.js とは

Vue.js をベースにした、フロントエンドのフレームワークです。

「フロントエンド」= ブラウザで動く、ユーザーが目にする部分。

Vue.js だけでもアプリは作れますが、Nuxt.js を使うと:
- ページのルーティング（URL の管理）が自動になる
- ファイルを作るだけでページができる
- SSR（サーバーサイドレンダリング）が使える

---

## ディレクトリ構成の意味

```
frontend/
├── pages/         ← URL に対応するページ。ファイルを作るだけでルーティング完成
│   └── index.vue  →  http://localhost:3000/  にアクセスしたときに表示される
├── components/    ← ボタン・カードなど再利用できる部品
├── layouts/       ← 全ページ共通の枠組み（ヘッダー・背景色など）
├── composables/   ← ページをまたいで使うロジック（例: useAuth）
├── stores/        ← アプリ全体で共有するデータ（例: ログイン状態）
├── types/         ← TypeScript の型定義
└── assets/css/    ← 全ページに適用する CSS
```

---

## Vue コンポーネントの基本構造

`.vue` というファイルが Vue のコンポーネント（部品）です。

```vue
<template>
  <!-- 画面に表示する HTML -->
  <main class="flex items-center justify-center min-h-screen">
    <h1 class="text-5xl text-green-400">GitQuest</h1>
  </main>
</template>

<script setup lang="ts">
// JavaScript (TypeScript) のロジック
// lang="ts" = TypeScript を使う宣言
</script>
```

`<template>` → 画面の見た目（HTML）
`<script setup>` → データや処理（JavaScript）
`<style scoped>` → このコンポーネントだけに適用する CSS（今回は Tailwind を使うので省略）

---

## Tailwind CSS とは

クラス名を HTML に書くだけでデザインできる CSS フレームワークです。

```html
<!-- 従来の書き方: CSS ファイルを別に作る -->
<h1 class="title">GitQuest</h1>
<style>
.title { font-size: 3rem; color: green; }
</style>

<!-- Tailwind の書き方: クラス名でそのままデザイン -->
<h1 class="text-5xl text-green-400">GitQuest</h1>
```

よく使うクラス名:

| クラス名 | 意味 |
|---------|------|
| `flex` | display: flex（横並び） |
| `items-center` | 縦方向中央揃え |
| `justify-center` | 横方向中央揃え |
| `min-h-screen` | 最低でも画面の高さいっぱい |
| `text-5xl` | 文字サイズ大きめ |
| `text-green-400` | 緑色のテキスト |
| `bg-gray-950` | とても暗いグレーの背景 |
| `px-4` | 左右に padding（余白） |

---

## nuxt.config.ts の読み方

```ts
export default defineNuxtConfig({
  // 開発ツールを有効化
  devtools: { enabled: true },

  // 使うモジュール（追加機能）
  modules: [
    '@pinia/nuxt',  // 状態管理ライブラリ Pinia
    '@nuxt/eslint', // コードの品質チェック
  ],

  // 全ページに適用する CSS
  css: ['~/assets/css/main.css'],

  // 設定値（コンポーネントから参照できる）
  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080/api',  // バックエンドの URL
    },
  },
})
```

`~/` は `frontend/` フォルダのルートを指す Nuxt の書き方です。

---

## package.json の読み方

```json
{
  "scripts": {
    "dev": "nuxt dev",       // 開発サーバー起動 → npm run dev
    "build": "nuxt build",   // 本番用ビルド
    "typecheck": "nuxt typecheck"  // 型チェック
  },
  "dependencies": {
    "nuxt": "^3.16.2"   // ^ = 3.16.2 以上の最新版を使う
  }
}
```

`npm install` を実行すると `dependencies` に書いたライブラリが全部インストールされます。

---

## TypeScript とは

JavaScript に「型」を追加した言語です。

```ts
// JavaScript: 型なし（何でも代入できてしまう）
let name = "kazuki"
name = 123  // エラーにならない → バグの原因になる

// TypeScript: 型あり
let name: string = "kazuki"
name = 123  // コンパイルエラー！ → バグを事前に発見できる
```

型を書くことで:
- 間違いをコード編集中に発見できる
- 「この変数は何が入るのか」がコードを読むだけでわかる

今回は `lang="ts"` と書くことで `.vue` ファイルでも TypeScript が使えます。

---

## Pinia（ピーニャ）とは

複数のページ・コンポーネントで共有するデータを管理するライブラリです。

例: ログイン状態はどのページからも参照したい。
→ `stores/auth.ts` に書いておけば全ページから使える。

```ts
// stores/auth.ts
export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)  // ログイン中のユーザー
  const isLoggedIn = computed(() => user.value !== null)
  return { user, isLoggedIn }
})
```

`ref()` = リアクティブな変数（値が変わると画面が自動更新される）
`computed()` = 別の値から自動計算される値
