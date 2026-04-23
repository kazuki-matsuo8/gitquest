<template>
  <div class="flex flex-col h-[calc(100vh-4rem)]">
    <!-- ヘッダー -->
    <div class="border-b border-gray-800 bg-gray-900 px-4 sm:px-6 py-4 shrink-0">
      <div class="max-w-7xl mx-auto flex items-center gap-4">
        <NuxtLink to="/missions" class="text-gray-400 hover:text-white transition-colors text-sm">
          ← ミッション一覧
        </NuxtLink>
        <div v-if="mission" class="flex items-center gap-3">
          <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-green-500/15 text-green-400">
            Lv.{{ mission.level }}
          </span>
          <h1 class="font-semibold text-gray-100">{{ mission.title }}</h1>
        </div>
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
import type { TerminalCommandResponse } from '~/types/terminal'

definePageMeta({ middleware: ['auth'] })

interface Mission {
  id: string
  level: number
  title: string
  description: string
  hint: string
}

const route = useRoute()
const config = useRuntimeConfig()
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

// ターミナルセッション
const sessionId = ref<string | null>(null)

async function createSession() {
  const res = await $fetch<{ sessionId: string }>(
    `${config.public.apiBase}/terminal/sessions`,
    { method: 'POST' }
  )
  sessionId.value = res.sessionId
}

// グラフデータ
const graphData = ref<TerminalCommandResponse['graph'] | null>(null)

function onGraphUpdated(graph: unknown) {
  graphData.value = graph as TerminalCommandResponse['graph']
}

// ページを離れるときにセッションを破棄
onUnmounted(async () => {
  if (sessionId.value) {
    await $fetch(`${config.public.apiBase}/terminal/sessions/${sessionId.value}`, {
      method: 'DELETE',
    }).catch(() => {})
  }
})

onMounted(createSession)
</script>
