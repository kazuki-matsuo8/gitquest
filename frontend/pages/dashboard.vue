<template>
  <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <!-- ヘッダー -->
    <div class="mb-10">
      <p class="text-gray-400 text-sm mb-1">おかえり、</p>
      <h1 class="text-3xl font-bold">{{ auth.username }}</h1>
    </div>

    <!-- ローディング -->
    <div v-if="pending" class="flex justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
    </div>

    <template v-else>
      <!-- サマリーカード -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="bg-gray-900 border border-gray-800 rounded-2xl p-5 flex flex-col gap-1"
        >
          <span class="text-2xl font-bold" :class="stat.color">{{ stat.value }}</span>
          <span class="text-xs text-gray-500">{{ stat.label }}</span>
        </div>
      </div>

      <!-- レベル別進捗 -->
      <section class="mb-10">
        <h2 class="text-lg font-semibold mb-5 text-gray-200">レベル別クリア率</h2>
        <div class="flex flex-col gap-4">
          <div
            v-for="lv in levelStats"
            :key="lv.level"
            class="bg-gray-900 border border-gray-800 rounded-2xl p-5"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <span :class="levelBadgeClass(lv.level)" class="text-xs font-bold px-3 py-1 rounded-full">
                  Lv.{{ lv.level }}
                </span>
                <span class="text-sm text-gray-300">{{ levelLabel(lv.level) }}</span>
              </div>
              <span class="text-sm font-semibold text-gray-300">{{ lv.cleared }} / {{ lv.total }}</span>
            </div>
            <!-- プログレスバー -->
            <div class="h-2 bg-gray-800 rounded-full overflow-hidden">
              <div
                class="h-full rounded-full transition-all duration-700"
                :class="lv.cleared === lv.total ? 'bg-green-500' : 'bg-blue-500'"
                :style="{ width: `${lv.total > 0 ? (lv.cleared / lv.total) * 100 : 0}%` }"
              />
            </div>
          </div>
        </div>
      </section>

      <!-- 最近クリアしたミッション -->
      <section v-if="recentCompleted.length > 0">
        <h2 class="text-lg font-semibold mb-5 text-gray-200">最近クリアしたミッション</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="p in recentCompleted"
            :key="p.id"
            class="bg-gray-900 border border-green-600/30 rounded-2xl p-5 flex flex-col gap-2"
          >
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold text-green-400">完了</span>
              <span class="text-xs text-gray-500">Lv.{{ p.missionLevel }}</span>
            </div>
            <p class="font-medium text-gray-100 text-sm">{{ p.missionTitle }}</p>
            <p class="text-xs text-gray-500">
              {{ formatDate(p.completedAt) }}
            </p>
          </div>
        </div>
      </section>

      <!-- まだ何もない場合 -->
      <div v-else-if="progressList.length === 0" class="text-center py-16">
        <p class="text-gray-500 mb-4">まだミッションを始めていません</p>
        <NuxtLink
          to="/missions"
          class="px-6 py-2.5 bg-green-600 hover:bg-green-500 text-white font-semibold rounded-xl transition-colors text-sm"
        >
          ミッション一覧へ
        </NuxtLink>
      </div>
    </template>
  </main>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ middleware: ['auth'] })

interface ProgressItem {
  id: string
  missionId: string
  missionTitle: string
  missionLevel: number
  status: 'IN_PROGRESS' | 'COMPLETED'
  completedAt: string | null
  updatedAt: string
}

interface Mission {
  id: string
  level: number
  title: string
}

const auth = useAuthStore()
const config = useRuntimeConfig()

const { data: progressRaw, pending } = await useFetch<ProgressItem[]>(
  `${config.public.apiBase}/progress`,
  { headers: { Authorization: `Bearer ${auth.token}` } }
)

const { data: missionsByLevel } = await useFetch<Record<string, Mission[]>>(
  `${config.public.apiBase}/missions`
)

const progressList = computed(() => progressRaw.value ?? [])

// 全ミッション数
const allMissions = computed(() => {
  if (!missionsByLevel.value) return []
  return Object.values(missionsByLevel.value).flat()
})

const completedCount = computed(() => progressList.value.filter((p) => p.status === 'COMPLETED').length)
const inProgressCount = computed(() => progressList.value.filter((p) => p.status === 'IN_PROGRESS').length)

const stats = computed(() => [
  { label: 'クリア済み', value: completedCount.value, color: 'text-green-400' },
  { label: '進行中', value: inProgressCount.value, color: 'text-blue-400' },
  { label: '総ミッション', value: allMissions.value.length, color: 'text-gray-300' },
  {
    label: 'クリア率',
    value: allMissions.value.length > 0
      ? `${Math.round((completedCount.value / allMissions.value.length) * 100)}%`
      : '0%',
    color: 'text-yellow-400',
  },
])

// レベル別進捗
const levelStats = computed(() => {
  if (!missionsByLevel.value) return []
  return Object.entries(missionsByLevel.value).map(([level, missions]) => {
    const cleared = missions.filter((m) =>
      progressList.value.some((p) => p.missionId === m.id && p.status === 'COMPLETED')
    ).length
    return { level: Number(level), total: missions.length, cleared }
  }).sort((a, b) => a.level - b.level)
})

// 最近クリアしたミッション（新しい順に最大 6 件）
const recentCompleted = computed(() =>
  progressList.value
    .filter((p) => p.status === 'COMPLETED' && p.completedAt)
    .sort((a, b) => new Date(b.completedAt!).getTime() - new Date(a.completedAt!).getTime())
    .slice(0, 6)
)

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

function formatDate(iso: string | null): string {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('ja-JP', {
    year: 'numeric', month: 'short', day: 'numeric',
  })
}
</script>
