<template>
  <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <!-- ヘッダー -->
    <div class="mb-8">
      <p class="text-gray-400 text-sm mb-1">おかえり、</p>
      <h1 class="text-3xl font-bold">{{ auth.username }}</h1>
    </div>

    <!-- ローディング -->
    <div v-if="pending" class="flex justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
    </div>

    <template v-else>
      <!-- ランク & XP カード -->
      <section class="mb-8 bg-gray-900 border border-gray-800 rounded-2xl p-6 sm:p-8 relative overflow-hidden">
        <div class="absolute -top-16 -right-16 w-64 h-64 bg-green-500/10 rounded-full blur-3xl pointer-events-none" />
        <div class="relative flex flex-col sm:flex-row sm:items-center gap-6">
          <!-- ランクアイコン -->
          <div class="w-20 h-20 rounded-2xl bg-gray-800 border border-gray-700 flex items-center justify-center text-4xl shrink-0 mx-auto sm:mx-0">
            {{ game.currentRank.value.icon }}
          </div>
          <div class="flex-1 text-center sm:text-left">
            <p class="text-xs text-gray-500 mb-1 tracking-wide uppercase">現在のランク</p>
            <h2 class="text-2xl font-bold text-gray-100 mb-3">{{ game.currentRank.value.name }}</h2>
            <!-- XP バー -->
            <div class="flex items-center gap-3">
              <div class="flex-1 h-2.5 bg-gray-800 rounded-full overflow-hidden">
                <div
                  class="h-full bg-linear-to-r from-green-600 to-green-400 rounded-full transition-all duration-700"
                  :style="{ width: `${game.rankProgress.value}%` }"
                />
              </div>
              <span class="text-sm font-semibold text-green-400 shrink-0">{{ game.totalXp.value }} XP</span>
            </div>
            <p v-if="game.nextRank.value" class="text-xs text-gray-500 mt-2">
              次のランク「{{ game.nextRank.value.icon }} {{ game.nextRank.value.name }}」まで
              あと {{ game.nextRank.value.minXp - game.totalXp.value }} XP
            </p>
            <p v-else class="text-xs text-yellow-400 mt-2">最高ランク到達！おめでとう 🎉</p>
          </div>
          <!-- ストリーク -->
          <div class="text-center bg-gray-800/60 border border-gray-700/50 rounded-2xl px-6 py-4 shrink-0 mx-auto sm:mx-0">
            <p class="text-3xl font-bold" :class="game.streak.value > 0 ? 'text-orange-400' : 'text-gray-600'">
              🔥 {{ game.streak.value }}
            </p>
            <p class="text-xs text-gray-500 mt-1">連続学習日数</p>
          </div>
        </div>
      </section>

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

      <!-- バッジ (実績) -->
      <section class="mb-10">
        <div class="flex items-center justify-between mb-5">
          <h2 class="text-lg font-semibold text-gray-200">実績バッジ</h2>
          <span class="text-sm text-gray-500">{{ game.earnedBadgeCount.value }} / {{ game.badges.value.length }} 獲得</span>
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          <div
            v-for="badge in game.badges.value"
            :key="badge.id"
            class="rounded-2xl p-5 border flex flex-col gap-2 transition-all"
            :class="badge.earned
              ? 'bg-gray-900 border-yellow-600/40 hover:border-yellow-500/60'
              : 'bg-gray-900/40 border-gray-800 opacity-50'"
          >
            <span class="text-3xl" :class="{ grayscale: !badge.earned }">{{ badge.icon }}</span>
            <p class="font-semibold text-sm" :class="badge.earned ? 'text-gray-100' : 'text-gray-500'">
              {{ badge.name }}
            </p>
            <p class="text-xs text-gray-500 leading-relaxed">{{ badge.description }}</p>
          </div>
        </div>
      </section>

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
              <span class="ml-auto text-xs font-semibold text-yellow-400">+{{ xpForLevel(p.missionLevel) }} XP</span>
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
import { useGameStats, xpForLevel } from '~/composables/useGameStats'

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
  orderIndex: number
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

const allMissions = computed(() => {
  if (!missionsByLevel.value) return []
  return Object.values(missionsByLevel.value).flat()
})

// ゲーミフィケーション統計
const game = useGameStats(
  () => progressList.value,
  () => allMissions.value,
)

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
    4: '上級 — 実践テクニック',
  }
  return labels[level] ?? `レベル ${level}`
}

function levelBadgeClass(level: number): string {
  const classes: Record<number, string> = {
    1: 'bg-green-500/15 text-green-400',
    2: 'bg-blue-500/15 text-blue-400',
    3: 'bg-purple-500/15 text-purple-400',
    4: 'bg-orange-500/15 text-orange-400',
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
