// ゲーミフィケーション (XP・ランク・バッジ・連続学習) の計算ロジック
// すべて進捗 API とミッション API のデータから導出する

export interface ProgressEntry {
  missionId: string
  status: 'IN_PROGRESS' | 'COMPLETED'
  completedAt: string | null
}

export interface MissionEntry {
  id: string
  level: number
  orderIndex: number
  title: string
}

export interface Rank {
  name: string
  icon: string
  minXp: number
}

export interface Badge {
  id: string
  icon: string
  name: string
  description: string
  earned: boolean
}

// ランク定義 (XP のしきい値順)
export const RANKS: Rank[] = [
  { name: 'みならい冒険者', icon: '🥚', minXp: 0 },
  { name: 'コミット使い', icon: '🌱', minXp: 300 },
  { name: 'ブランチ騎士', icon: '🛡️', minXp: 800 },
  { name: 'マージ魔導士', icon: '🔮', minXp: 1500 },
  { name: 'Git マスター', icon: '👑', minXp: 2400 },
]

// ミッション 1 件あたりの XP はレベル × 100
export function xpForLevel(level: number): number {
  return level * 100
}

export function useGameStats(
  progress: () => ProgressEntry[],
  missions: () => MissionEntry[],
) {
  const completedMissions = computed(() => {
    const completedIds = new Set(
      progress().filter((p) => p.status === 'COMPLETED').map((p) => p.missionId),
    )
    return missions().filter((m) => completedIds.has(m.id))
  })

  // ─── XP とランク ───

  const totalXp = computed(() =>
    completedMissions.value.reduce((sum, m) => sum + xpForLevel(m.level), 0),
  )

  const currentRank = computed(() => {
    let rank = RANKS[0]
    for (const r of RANKS) {
      if (totalXp.value >= r.minXp) rank = r
    }
    return rank
  })

  const nextRank = computed(() => {
    const idx = RANKS.indexOf(currentRank.value)
    return idx < RANKS.length - 1 ? RANKS[idx + 1] : null
  })

  // 次のランクまでの進捗率 (0〜100)
  const rankProgress = computed(() => {
    if (!nextRank.value) return 100
    const span = nextRank.value.minXp - currentRank.value.minXp
    const gained = totalXp.value - currentRank.value.minXp
    return Math.min(100, Math.round((gained / span) * 100))
  })

  // ─── バッジ ───

  const badges = computed<Badge[]>(() => {
    const all = missions()
    const done = completedMissions.value
    const levelDone = (lv: number) => {
      const inLevel = all.filter((m) => m.level === lv)
      return inLevel.length > 0 && inLevel.every((m) => done.some((d) => d.id === m.id))
    }
    const missionDone = (lv: number, order: number) =>
      done.some((m) => m.level === lv && m.orderIndex === order)

    return [
      {
        id: 'first-step', icon: '🎯', name: 'はじめの一歩',
        description: '最初のミッションをクリア', earned: done.length >= 1,
      },
      {
        id: 'lv1-clear', icon: '🌱', name: 'Git 入門者',
        description: 'Lv1 のミッションをすべてクリア', earned: levelDone(1),
      },
      {
        id: 'lv2-clear', icon: '🌿', name: 'ブランチ使い',
        description: 'Lv2 のミッションをすべてクリア', earned: levelDone(2),
      },
      {
        id: 'lv3-clear', icon: '🔍', name: '履歴の探偵',
        description: 'Lv3 のミッションをすべてクリア', earned: levelDone(3),
      },
      {
        id: 'conflict-buster', icon: '⚔️', name: 'コンフリクトバスター',
        description: 'マージコンフリクトを解決', earned: missionDone(4, 1),
      },
      {
        id: 'time-traveler', icon: '⏪', name: 'タイムトラベラー',
        description: 'コミットの修正・取り消しを習得', earned: missionDone(4, 2) && missionDone(4, 3),
      },
      {
        id: 'all-clear', icon: '👑', name: 'GitQuest マスター',
        description: '全ミッションをクリア',
        earned: all.length > 0 && done.length === all.length,
      },
    ]
  })

  const earnedBadgeCount = computed(() => badges.value.filter((b) => b.earned).length)

  // ─── 連続学習日数 (ストリーク) ───

  const streak = computed(() => {
    const days = new Set(
      progress()
        .filter((p) => p.status === 'COMPLETED' && p.completedAt)
        .map((p) => new Date(p.completedAt!).toDateString()),
    )
    if (days.size === 0) return 0

    let count = 0
    const cursor = new Date()
    // 今日まだクリアしていなくても、昨日までの連続は維持扱いにする
    if (!days.has(cursor.toDateString())) cursor.setDate(cursor.getDate() - 1)

    while (days.has(cursor.toDateString())) {
      count++
      cursor.setDate(cursor.getDate() - 1)
    }
    return count
  })

  return { totalXp, currentRank, nextRank, rankProgress, badges, earnedBadgeCount, streak }
}
