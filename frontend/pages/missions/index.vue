<template>
  <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <!-- ヘッダー -->
    <div class="mb-10">
      <h1 class="text-3xl font-bold mb-2">ミッション一覧</h1>
      <p class="text-gray-400">レベルごとにミッションをクリアして Git マスターになろう</p>
    </div>

    <!-- ローディング -->
    <div v-if="pending" class="flex justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
    </div>

    <!-- エラー -->
    <div v-else-if="error" class="bg-red-500/10 border border-red-500/30 text-red-400 rounded-xl px-6 py-4">
      ミッションの取得に失敗しました
    </div>

    <!-- 全体進捗バー -->
    <div v-if="!pending && !error" class="mb-8 bg-gray-900 border border-gray-800 rounded-2xl p-5">
      <div class="flex items-center justify-between mb-3">
        <span class="text-sm font-medium text-gray-300">全体の進捗</span>
        <span class="text-sm font-semibold text-green-400">{{ completedCount }} / {{ totalCount }} クリア</span>
      </div>
      <div class="h-2.5 bg-gray-800 rounded-full overflow-hidden">
        <div
          class="h-full bg-gradient-to-r from-green-600 to-green-400 rounded-full transition-all duration-700"
          :style="{ width: `${totalCount > 0 ? (completedCount / totalCount) * 100 : 0}%` }"
        />
      </div>
    </div>

    <!-- ミッション一覧 -->
    <div v-if="!pending && !error" class="flex flex-col gap-10">
      <section
        v-for="(missions, level) in missionsByLevel"
        :key="level"
      >
        <!-- レベルヘッダー -->
        <div class="flex items-center gap-3 mb-5">
          <span
            :class="levelBadgeClass(Number(level))"
            class="text-xs font-bold px-3 py-1 rounded-full"
          >
            Lv.{{ level }}
          </span>
          <h2 class="text-lg font-semibold text-gray-200">{{ levelLabel(Number(level)) }}</h2>
          <div class="flex-1 h-px bg-gray-800" />
        </div>

        <!-- ミッションカード -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="mission in missions"
            :key="mission.id"
            class="bg-gray-900 border rounded-2xl p-6 flex flex-col gap-3 hover:border-green-500/50 transition-all hover:-translate-y-0.5"
            :class="progressStatus(mission.id) === 'COMPLETED' ? 'border-green-600/40' : 'border-gray-800'"
          >
            <!-- ステータスバッジ -->
            <div class="flex items-center justify-between">
              <span
                :class="statusClass(progressStatus(mission.id))"
                class="text-xs font-semibold px-2.5 py-1 rounded-full"
              >
                {{ statusLabel(progressStatus(mission.id)) }}
              </span>
            </div>

            <h3 class="font-semibold text-gray-100">{{ mission.title }}</h3>
            <p class="text-sm text-gray-400 leading-relaxed flex-1">{{ mission.description }}</p>

            <!-- ヒント -->
            <div class="bg-gray-800 rounded-lg px-3 py-2 flex items-center gap-2">
              <span class="text-xs text-gray-500">ヒント:</span>
              <code class="text-xs text-green-400 font-mono">{{ mission.hint }}</code>
            </div>

            <!-- ボタン -->
            <NuxtLink
              :to="`/missions/${mission.id}`"
              class="mt-1 w-full text-sm font-semibold py-2.5 rounded-xl transition-all text-center block"
              :class="progressStatus(mission.id) === 'COMPLETED'
                ? 'bg-green-600/20 text-green-400 hover:bg-green-600/30'
                : progressStatus(mission.id) === 'IN_PROGRESS'
                  ? 'bg-blue-600 hover:bg-blue-500 text-white'
                  : 'bg-gray-800 hover:bg-gray-700 text-gray-200'"
              @click="progressStatus(mission.id) === 'NOT_STARTED' && handleStart(mission.id)"
            >
              {{ progressStatus(mission.id) === 'COMPLETED' ? '完了 — 再挑戦' : progressStatus(mission.id) === 'IN_PROGRESS' ? '続ける' : 'スタート' }}
            </NuxtLink>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

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

const auth = useAuthStore()
const config = useRuntimeConfig()

// ミッション一覧を取得
const { data: missionsByLevel, pending, error } = await useFetch<Record<string, Mission[]>>(
  `${config.public.apiBase}/missions`,
)

// 自分の進捗を取得
const progressList = ref<ProgressItem[]>([])

async function fetchProgress() {
  if (!auth.token) return
  const data = await $fetch<ProgressItem[]>(`${config.public.apiBase}/progress`, {
    headers: { Authorization: `Bearer ${auth.token}` },
  })
  progressList.value = data
}

onMounted(fetchProgress)

// 全ミッション数・クリア数
const totalCount = computed(() => {
  if (!missionsByLevel.value) return 0
  return Object.values(missionsByLevel.value).flat().length
})
const completedCount = computed(() =>
  progressList.value.filter((p) => p.status === 'COMPLETED').length
)

// ミッション ID からステータスを返す
function progressStatus(missionId: string): string {
  return progressList.value.find((p) => p.missionId === missionId)?.status ?? 'NOT_STARTED'
}

async function handleStart(missionId: string) {
  if (!auth.token) return
  await $fetch(`${config.public.apiBase}/progress/${missionId}/start`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${auth.token}` },
  })
  await fetchProgress()
}

function levelLabel(level: number): string {
  const labels: Record<number, string> = {
    1: '入門 — 基本操作',
    2: '初級 — ブランチ操作',
    3: '中級 — 履歴・差分確認',
  }
  return labels[level] ?? `レベル ${level}`
}

function levelBadgeClass(level: number): string {
  const classes: Record<number, string> = {
    1: 'bg-green-500/15 text-green-400',
    2: 'bg-blue-500/15 text-blue-400',
    3: 'bg-purple-500/15 text-purple-400',
  }
  return classes[level] ?? 'bg-gray-700 text-gray-300'
}

function statusLabel(status: string): string {
  const labels: Record<string, string> = {
    NOT_STARTED: '未着手',
    IN_PROGRESS: '進行中',
    COMPLETED: '完了',
  }
  return labels[status] ?? '未着手'
}

function statusClass(status: string): string {
  const classes: Record<string, string> = {
    NOT_STARTED: 'bg-gray-700/50 text-gray-400',
    IN_PROGRESS: 'bg-blue-500/15 text-blue-400',
    COMPLETED: 'bg-green-500/15 text-green-400',
  }
  return classes[status] ?? 'bg-gray-700/50 text-gray-400'
}
</script>
