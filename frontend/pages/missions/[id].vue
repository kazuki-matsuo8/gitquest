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

    <!-- ヘッダー -->
    <div class="border-b border-gray-800 bg-gray-900 px-4 sm:px-6 py-4 shrink-0">
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4">
          <NuxtLink to="/missions" class="text-gray-400 hover:text-white transition-colors text-sm">
            ← 一覧
          </NuxtLink>
          <div v-if="mission" class="flex items-center gap-3">
            <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-green-500/15 text-green-400">
              Lv.{{ mission.level }}
            </span>
            <h1 class="font-semibold text-gray-100 text-sm sm:text-base">{{ mission.title }}</h1>
          </div>
        </div>

        <!-- 完了ボタン -->
        <button
          v-if="progressStatus !== 'COMPLETED'"
          :disabled="completing"
          class="text-sm font-semibold px-4 py-2 rounded-xl transition-all"
          :class="progressStatus === 'IN_PROGRESS'
            ? 'bg-green-600 hover:bg-green-500 text-white'
            : 'bg-gray-800 hover:bg-gray-700 text-gray-300'"
          @click="handleComplete"
        >
          {{ completing ? '記録中...' : 'ミッション完了' }}
        </button>
        <span
          v-else
          class="text-sm font-semibold px-4 py-2 rounded-xl bg-green-600/20 text-green-400"
        >
          完了済み
        </span>
      </div>
    </div>

    <!-- メインレイアウト -->
    <div class="flex-1 min-h-0 max-w-7xl mx-auto w-full px-4 sm:px-6 py-4 flex flex-col lg:flex-row gap-4">

      <!-- 左: ミッション説明 + ターミナル -->
      <div class="flex flex-col gap-4 w-full lg:w-1/2 min-h-0">
        <!-- ミッション説明カード -->
        <div v-if="mission" class="bg-gray-900 border border-gray-800 rounded-xl p-5 shrink-0">
          <p class="text-gray-300 text-sm leading-relaxed mb-3">{{ mission.description }}</p>
          <div class="flex items-center gap-2 bg-gray-800 rounded-lg px-3 py-2">
            <span class="text-xs text-gray-500">ヒント:</span>
            <code class="text-xs text-green-400 font-mono">{{ mission.hint }}</code>
          </div>
        </div>

        <!-- ターミナル -->
        <div class="flex-1 min-h-0">
          <TerminalPane
            :session-id="sessionId"
            @graph-updated="onGraphUpdated"
          />
        </div>
      </div>

      <!-- 右: コミットグラフ -->
      <div class="w-full lg:w-1/2 min-h-[300px] lg:min-h-0">
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
  title: string
  description: string
  hint: string
}

interface ProgressItem {
  id: string
  missionId: string
  status: 'IN_PROGRESS' | 'COMPLETED'
}

const route = useRoute()
const config = useRuntimeConfig()
const auth = useAuthStore()
const missionId = route.params.id as string

// ミッション情報を取得
const { data: missionsByLevel } = await useFetch<Record<string, Mission[]>>(
  `${config.public.apiBase}/missions`
)
const mission = computed(() => {
  if (!missionsByLevel.value) return null
  for (const missions of Object.values(missionsByLevel.value)) {
    const found = missions.find((m) => m.id === missionId)
    if (found) return found
  }
  return null
})

// 進捗状態
const progressStatus = ref<string>('NOT_STARTED')

async function fetchProgress() {
  if (!auth.token) return
  const list = await $fetch<ProgressItem[]>(`${config.public.apiBase}/progress`, {
    headers: { Authorization: `Bearer ${auth.token}` },
  }).catch(() => [] as ProgressItem[])
  const found = list.find((p) => p.missionId === missionId)
  progressStatus.value = found?.status ?? 'NOT_STARTED'
}

// ターミナルセッション
const sessionId = ref<string | null>(null)

async function createSession() {
  const res = await $fetch<{ sessionId: string }>(
    `${config.public.apiBase}/terminal/sessions`,
    { method: 'POST' }
  )
  sessionId.value = res.sessionId

  // セッション作成と同時に「開始中」にする
  if (auth.token && progressStatus.value === 'NOT_STARTED') {
    await $fetch(`${config.public.apiBase}/progress/${missionId}/start`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.token}` },
    }).catch(() => {})
    progressStatus.value = 'IN_PROGRESS'
  }
}

// グラフデータ
const graphData = ref<GraphData | null>(null)

function onGraphUpdated(graph: unknown) {
  graphData.value = graph as GraphData
}

// ミッション完了
const completing = ref(false)
const showCelebration = ref(false)

async function handleComplete() {
  if (!auth.token || completing.value) return
  completing.value = true
  try {
    await $fetch(`${config.public.apiBase}/progress/${missionId}/complete`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.token}` },
    })
    progressStatus.value = 'COMPLETED'
    showCelebration.value = true
  } catch {
    // 失敗しても静かに無視
  } finally {
    completing.value = false
  }
}

// ページを離れるときにセッションを破棄
onUnmounted(async () => {
  if (sessionId.value) {
    await $fetch(`${config.public.apiBase}/terminal/sessions/${sessionId.value}`, {
      method: 'DELETE',
    }).catch(() => {})
  }
})

onMounted(async () => {
  await fetchProgress()
  await createSession()
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
