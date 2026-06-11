<template>
  <div class="flex flex-col h-[calc(100vh-4rem)] relative">
    <!-- 完了お祝いオーバーレイ -->
    <Transition name="celebration">
      <div
        v-if="showCelebration"
        class="absolute inset-0 z-50 flex items-center justify-center bg-gray-950/90 backdrop-blur-sm"
      >
        <div class="text-center px-8">
          <div class="text-6xl mb-4 animate-bounce">🎉</div>
          <h2 class="text-3xl font-bold text-green-400 mb-2">ミッション完了！</h2>
          <p class="text-gray-300 mb-8">{{ mission?.title }}</p>
          <div class="flex flex-col sm:flex-row gap-3 justify-center">
            <NuxtLink
              to="/missions"
              class="px-6 py-3 bg-green-600 hover:bg-green-500 text-white font-semibold rounded-xl transition-colors"
            >
              ミッション一覧へ
            </NuxtLink>
            <button
              class="px-6 py-3 bg-gray-800 hover:bg-gray-700 text-gray-200 font-semibold rounded-xl transition-colors"
              @click="showCelebration = false"
            >
              続ける
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ページヘッダー -->
    <div class="border-b border-gray-800 bg-gray-900 px-4 sm:px-6 py-4 shrink-0">
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4 min-w-0">
          <NuxtLink to="/missions" class="text-gray-400 hover:text-white transition-colors text-sm shrink-0">
            ← 一覧
          </NuxtLink>
          <div v-if="mission" class="flex items-center gap-3 min-w-0">
            <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-green-500/15 text-green-400 shrink-0">
              Lv.{{ mission.level }}
            </span>
            <h1 class="font-semibold text-gray-100 text-sm sm:text-base truncate">{{ mission.title }}</h1>
          </div>
        </div>
        <span
          v-if="progressStatus === 'COMPLETED'"
          class="text-sm font-semibold px-4 py-2 rounded-xl bg-green-600/20 text-green-400 shrink-0"
        >
          完了済み
        </span>
      </div>
    </div>

    <!-- ミッション説明カード (常時表示) -->
    <div v-if="mission" class="shrink-0 px-4 sm:px-6 pt-4 max-w-7xl mx-auto w-full">
      <div class="bg-gray-900 border border-gray-800 rounded-xl p-4 sm:p-5">
        <p class="text-gray-300 text-sm leading-relaxed mb-3">{{ mission.description }}</p>
        <div>
          <button
            class="text-xs text-gray-500 hover:text-gray-300 transition-colors flex items-center gap-1"
            @click="showHint = !showHint"
          >
            <span>{{ showHint ? '▼' : '▶' }}</span>
            <span>ヒントを{{ showHint ? '隠す' : '見る' }}</span>
          </button>
          <div v-if="showHint" class="mt-2 flex items-center gap-2 bg-gray-800 rounded-lg px-3 py-2">
            <span class="text-xs text-gray-500 shrink-0">ヒント:</span>
            <code class="text-xs text-green-400 font-mono break-all">{{ mission.hint }}</code>
          </div>
        </div>
      </div>
    </div>

    <!-- モバイル用タブ切り替え (lg 未満のみ表示) -->
    <div class="lg:hidden shrink-0 px-4 sm:px-6 pt-3 max-w-7xl mx-auto w-full">
      <div class="flex rounded-xl overflow-hidden border border-gray-800">
        <button
          class="flex-1 py-2.5 text-sm font-medium transition-colors"
          :class="activeTab === 'terminal'
            ? 'bg-gray-800 text-white'
            : 'text-gray-500 hover:text-gray-400 bg-gray-900'"
          @click="activeTab = 'terminal'"
        >
          ターミナル
        </button>
        <button
          class="flex-1 py-2.5 text-sm font-medium transition-colors"
          :class="activeTab === 'graph'
            ? 'bg-gray-800 text-white'
            : 'text-gray-500 hover:text-gray-400 bg-gray-900'"
          @click="activeTab = 'graph'"
        >
          グラフ{{ graphData && graphData.commits.length > 0 ? ` (${graphData.commits.length})` : '' }}
        </button>
      </div>
    </div>

    <!-- メインコンテンツ -->
    <div class="flex-1 min-h-0 max-w-7xl mx-auto w-full px-4 sm:px-6 py-4 flex flex-col lg:flex-row gap-4">

      <!-- ターミナル (xterm.js) -->
      <div
        class="min-h-0 lg:w-1/2 rounded-xl overflow-hidden border border-gray-800"
        :class="activeTab === 'terminal'
          ? 'flex flex-col flex-1'
          : 'hidden lg:flex lg:flex-col'"
      >
        <ClientOnly>
          <XTermPane
            v-if="sessionId"
            :session-id="sessionId"
            :api-base="config.public.apiBase"
            :on-graph-update="fetchGraph"
          />
          <template #fallback>
            <div class="flex-1 flex items-center justify-center text-gray-500 text-sm bg-gray-950">
              読み込み中…
            </div>
          </template>
        </ClientOnly>
        <div v-if="!sessionId" class="flex-1 flex items-center justify-center text-gray-500 text-sm bg-gray-950">
          セッション準備中…
        </div>
      </div>

      <!-- コミットグラフ -->
      <div
        class="min-h-0 lg:w-1/2"
        :class="activeTab === 'graph'
          ? 'flex flex-col flex-1'
          : 'hidden lg:flex lg:flex-col'"
      >
        <CommitGraph :graph="graphData" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import type { GraphData } from '~/types/terminal'

definePageMeta({ middleware: ['auth'] })

interface Mission {
  id: string
  level: number
  orderIndex: number
  title: string
  description: string
  hint: string
}

interface ProgressItem {
  id: string
  missionId: string
  status: 'IN_PROGRESS' | 'COMPLETED'
}

const route  = useRoute()
const config = useRuntimeConfig()
const auth   = useAuthStore()
const missionId = route.params.id as string

const activeTab = ref<'terminal' | 'graph'>('terminal')

const { data: missionsByLevel } = await useFetch<Record<string, Mission[]>>(
  `${config.public.apiBase}/missions`
)
const mission = computed<Mission | null>(() => {
  if (!missionsByLevel.value) return null
  for (const missions of Object.values(missionsByLevel.value)) {
    const found = missions.find((m) => m.id === missionId)
    if (found) return found
  }
  return null
})

const progressStatus = ref<string>('NOT_STARTED')
const showHint        = ref(false)
const showCelebration = ref(false)

async function fetchProgress() {
  if (!auth.token) return
  const list = await $fetch<ProgressItem[]>(`${config.public.apiBase}/progress`, {
    headers: { Authorization: `Bearer ${auth.token}` },
  }).catch(() => [] as ProgressItem[])
  const found = list.find((p) => p.missionId === missionId)
  progressStatus.value = found?.status ?? 'NOT_STARTED'
}

// ────────────────────────────────────────────
// セッション
// ────────────────────────────────────────────

const sessionId = ref<string | null>(null)

async function createSession() {
  const res = await $fetch<{ sessionId: string; setupMessage: string }>(
    `${config.public.apiBase}/terminal/sessions`,
    { method: 'POST', body: { missionId } }
  )
  sessionId.value = res.sessionId

  if (auth.token && progressStatus.value === 'NOT_STARTED') {
    await $fetch(`${config.public.apiBase}/progress/${missionId}/start`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.token}` },
    }).catch(() => {})
    progressStatus.value = 'IN_PROGRESS'
  }
}

// ────────────────────────────────────────────
// グラフ取得 (OSC 9999 → XTermPane が呼ぶ)
// ────────────────────────────────────────────

const graphData = ref<GraphData | null>(null)

async function fetchGraph() {
  if (!sessionId.value) return
  try {
    const data = await $fetch<GraphData>(
      `${config.public.apiBase}/terminal/sessions/${sessionId.value}/graph`
    )
    graphData.value = data
    await checkMissionCompletion()
  } catch {
    // 無視
  }
}

// ────────────────────────────────────────────
// ミッション完了チェック (サーバーサイドで実状態を判定)
// ────────────────────────────────────────────

async function checkMissionCompletion() {
  if (!sessionId.value || progressStatus.value === 'COMPLETED') return
  try {
    const res = await $fetch<{ completed: boolean }>(
      `${config.public.apiBase}/terminal/sessions/${sessionId.value}/check`,
      { params: { missionId } }
    )
    if (res.completed) {
      await onMissionCompleted()
    }
  } catch {
    // 無視
  }
}

async function onMissionCompleted() {
  if (progressStatus.value === 'COMPLETED') return
  if (!auth.token) return
  try {
    await $fetch(`${config.public.apiBase}/progress/${missionId}/complete`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.token}` },
    })
    progressStatus.value = 'COMPLETED'
    showCelebration.value = true
  } catch {
    // 静かに無視
  }
}

// ────────────────────────────────────────────
// ライフサイクル
// ────────────────────────────────────────────

onMounted(async () => {
  await fetchProgress()
  await createSession()
})

onUnmounted(async () => {
  if (sessionId.value) {
    await $fetch(`${config.public.apiBase}/terminal/sessions/${sessionId.value}`, {
      method: 'DELETE',
    }).catch(() => {})
  }
})
</script>

<style scoped>
.celebration-enter-active,
.celebration-leave-active {
  transition: opacity 0.3s ease;
}
.celebration-enter-from,
.celebration-leave-to {
  opacity: 0;
}
</style>
