# Vue.js / Nuxt.js 基本文法

## .vue ファイルの構造

```vue
<template>
  <!-- 画面に表示する HTML -->
</template>

<script setup lang="ts">
// TypeScript のロジック
</script>

<style scoped>
/* このコンポーネントだけに適用する CSS（Tailwind を使うなら基本不要）*/
</style>
```

`<script setup>` = Vue 3 の書き方。クラスやコンストラクタが不要でシンプル。
`lang="ts"` = TypeScript を使う宣言。

---

## リアクティブな変数

「値が変わったら画面が自動更新される」変数のことです。

```ts
import { ref, reactive, computed } from 'vue'

// ref: 数値・文字列・真偽値などシンプルな値
const count = ref(0)
const username = ref('')
const isLoading = ref(false)

// スクリプト内では .value でアクセス
count.value++
username.value = 'kazuki'

// テンプレートでは .value 不要（自動で外れる）
// <p>{{ count }}</p>  → 0, 1, 2...と自動更新

// reactive: オブジェクト
const user = reactive({
  name: 'kazuki',
  email: 'kazuki@example.com'
})
user.name = 'tanaka' // 直接変更できる

// computed: 他の値から自動計算される（変更不可）
const fullLabel = computed(() => `Lv.${level.value}: ${title.value}`)
```

---

## テンプレート構文

```vue
<template>
  <!-- {{ }} でデータを表示 -->
  <p>{{ username }}</p>

  <!-- v-if: 条件によって表示/非表示 -->
  <p v-if="isLoggedIn">ログイン中</p>
  <p v-else>ログインしてください</p>

  <!-- v-for: リストを繰り返す -->
  <ul>
    <li v-for="mission in missions" :key="mission.id">
      {{ mission.title }}
    </li>
  </ul>

  <!-- : (v-bind): 属性にデータを渡す -->
  <img :src="user.avatarUrl" :alt="user.name" />

  <!-- @ (v-on): イベントを受け取る -->
  <button @click="handleClick">クリック</button>
  <input @input="handleInput" v-model="inputValue" />

  <!-- v-model: 双方向バインディング（input の値と変数を同期） -->
  <input v-model="username" placeholder="ユーザー名" />
  <p>入力中: {{ username }}</p>
</template>
```

---

## イベントハンドラ

```vue
<script setup lang="ts">
const count = ref(0)

// クリックされたら呼ばれる関数
function increment() {
  count.value++
}

// 引数ありの関数
function handleMissionClick(missionId: string) {
  console.log('クリックされたミッション:', missionId)
  navigateTo(`/missions/${missionId}`)
}
</script>

<template>
  <button @click="increment">{{ count }}</button>
  <button @click="handleMissionClick(mission.id)">ミッション開始</button>
</template>
```

---

## Props（親から子への値渡し）

コンポーネントに外からデータを渡す仕組みです。

```vue
<!-- 子コンポーネント: MissionCard.vue -->
<script setup lang="ts">
// 受け取るデータの型を定義
interface Props {
  title: string
  level: number
  isCompleted: boolean
}

const props = defineProps<Props>()
</script>

<template>
  <div class="card">
    <span>Lv.{{ props.level }}</span>
    <h3>{{ props.title }}</h3>
    <span v-if="props.isCompleted">✅ 完了</span>
  </div>
</template>
```

```vue
<!-- 親コンポーネント: pages/missions/index.vue -->
<template>
  <!-- : をつけて動的な値を渡す -->
  <MissionCard
    :title="mission.title"
    :level="mission.level"
    :is-completed="mission.isCompleted"
  />
</template>
```

---

## Emit（子から親への通知）

子コンポーネントから親に「〇〇が起きた」と伝える仕組みです。

```vue
<!-- 子コンポーネント -->
<script setup lang="ts">
const emit = defineEmits<{
  start: [missionId: string]  // 'start' イベントを定義
}>()

function handleStart() {
  emit('start', 'mission-123') // 親に通知
}
</script>
```

```vue
<!-- 親コンポーネント -->
<MissionCard @start="(id) => startMission(id)" />
```

---

## useFetch（API 通信）

Nuxt の組み込み機能でバックエンド API を叩きます。

```ts
// GET リクエスト
const { data, pending, error } = await useFetch('/api/missions', {
  baseURL: useRuntimeConfig().public.apiBase,
  headers: {
    Authorization: `Bearer ${token.value}`
  }
})

// data.value に結果が入る
// pending.value = true の間はローディング中
// error.value にエラーが入る
```

```vue
<template>
  <!-- ローディング中 -->
  <div v-if="pending">読み込み中...</div>

  <!-- エラー -->
  <div v-else-if="error">エラーが発生しました</div>

  <!-- データ表示 -->
  <div v-else>
    <MissionCard
      v-for="mission in data"
      :key="mission.id"
      :title="mission.title"
    />
  </div>
</template>
```

---

## Pinia（状態管理）

複数ページで共有するデータを管理します。

```ts
// stores/auth.ts
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // state: 管理するデータ
  const token = ref<string | null>(null)
  const user = ref<{ id: string; username: string } | null>(null)

  // getters: 算出された値
  const isLoggedIn = computed(() => token.value !== null)

  // actions: データを変更する関数
  function setToken(newToken: string) {
    token.value = newToken
    // localStorage に保存（ページ更新後も保持）
    localStorage.setItem('token', newToken)
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, isLoggedIn, setToken, logout }
})
```

```vue
<!-- どのページ・コンポーネントからでも使える -->
<script setup lang="ts">
const auth = useAuthStore()
</script>

<template>
  <button v-if="auth.isLoggedIn" @click="auth.logout()">ログアウト</button>
  <span v-else>未ログイン</span>
</template>
```

---

## Nuxt のルーティング

`pages/` のファイル構成が URL になります。

```
pages/
├── index.vue          → /               （トップページ）
├── login.vue          → /login          （ログインページ）
├── register.vue       → /register       （登録ページ）
└── missions/
    ├── index.vue      → /missions        （ミッション一覧）
    └── [id].vue       → /missions/abc123 （ミッション詳細）
```

動的ルート（`[id].vue`）でパラメータを受け取る:

```ts
// pages/missions/[id].vue
const route = useRoute()
const missionId = route.params.id // URL の {id} 部分が入る
```

ページ遷移:

```ts
// スクリプト内で移動
navigateTo('/missions')
navigateTo(`/missions/${id}`)

// テンプレートでリンク
// <NuxtLink to="/missions">ミッション一覧へ</NuxtLink>
// <NuxtLink :to="`/missions/${id}`">詳細へ</NuxtLink>
```

---

## TypeScript の型定義

```ts
// types/mission.ts に型をまとめて書く

// interface: オブジェクトの型
interface Mission {
  id: string
  level: number
  title: string
  description: string
  hint: string | null  // null かもしれない場合は | null
  steps: MissionStep[]
}

interface MissionStep {
  id: string
  orderIndex: number
  description: string
  expectedCommand: string
}

// 使い方
const mission = ref<Mission | null>(null)
const missions = ref<Mission[]>([])
```

`string | null` = string か null どちらかが入る（Union 型）
`Mission[]` = Mission の配列
`ref<Mission | null>(null)` = Mission か null が入る ref（初期値は null）
