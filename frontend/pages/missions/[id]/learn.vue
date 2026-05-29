<template>
  <div class="min-h-screen bg-gray-950 flex flex-col">

    <!-- ヘッダー -->
    <header class="border-b border-gray-800 bg-gray-900 px-4 sm:px-6 py-4 shrink-0">
      <div class="max-w-3xl mx-auto flex items-center gap-4">
        <NuxtLink to="/missions" class="text-gray-400 hover:text-white transition-colors text-sm">
          ← 一覧
        </NuxtLink>
        <div v-if="mission" class="flex items-center gap-3 min-w-0">
          <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-green-500/15 text-green-400 shrink-0">
            Lv.{{ mission.level }}
          </span>
          <span class="text-gray-400 text-sm truncate">{{ mission.title }}</span>
        </div>
      </div>
    </header>

    <!-- コンテンツ -->
    <main class="flex-1 px-4 sm:px-6 py-10">
      <div class="max-w-3xl mx-auto">

        <!-- 学習バッジ -->
        <div class="flex items-center gap-2 mb-6">
          <span class="text-xs font-semibold px-3 py-1 rounded-full bg-blue-500/15 text-blue-400 border border-blue-500/20">
            📖 解説
          </span>
          <span v-if="mission" class="text-xs text-gray-500">
            演習「{{ mission.title }}」の前に読んでおこう
          </span>
        </div>

        <!-- Markdown コンテンツ -->
        <article
          v-if="mission?.learningContent"
          class="prose-custom"
          v-html="renderedContent"
        />
        <div v-else class="text-gray-500 text-sm">読み込み中…</div>

        <!-- 区切り -->
        <div class="mt-12 border-t border-gray-800 pt-8">
          <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div>
              <p class="text-sm text-gray-400">理解できたら演習に進もう</p>
              <p class="text-xs text-gray-600 mt-1">ターミナルで実際にコマンドを打って体験できます</p>
            </div>
            <NuxtLink
              v-if="mission"
              :to="`/missions/${mission.id}`"
              class="shrink-0 px-6 py-3 bg-green-600 hover:bg-green-500 text-white font-semibold rounded-xl transition-colors flex items-center gap-2"
            >
              演習をはじめる
              <span>→</span>
            </NuxtLink>
          </div>
        </div>

      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { marked } from 'marked'

definePageMeta({ middleware: ['auth'] })

interface Mission {
  id: string
  level: number
  orderIndex: number
  title: string
  description: string
  hint: string
  learningContent: string | null
}

const route  = useRoute()
const config = useRuntimeConfig()
const missionId = route.params.id as string

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

const renderedContent = computed(() => {
  if (!mission.value?.learningContent) return ''
  return marked(mission.value.learningContent) as string
})

// SEO
useHead({
  title: computed(() => mission.value ? `${mission.value.title} — 解説 | GitQuest` : 'GitQuest'),
})
</script>

<style>
/* Markdown コンテンツのスタイル */
.prose-custom {
  color: #d1d5db; /* gray-300 */
  line-height: 1.8;
}

.prose-custom h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #f9fafb;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #1f2937;
}

.prose-custom h2 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #f3f4f6;
  margin-top: 2rem;
  margin-bottom: 0.75rem;
}

.prose-custom h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #e5e7eb;
  margin-top: 1.5rem;
  margin-bottom: 0.5rem;
}

.prose-custom p {
  margin-bottom: 1rem;
}

.prose-custom ul, .prose-custom ol {
  margin-bottom: 1rem;
  padding-left: 1.5rem;
}

.prose-custom li {
  margin-bottom: 0.25rem;
}

.prose-custom code {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.875em;
  background: #111827;
  color: #4ade80;
  padding: 0.15em 0.4em;
  border-radius: 0.25rem;
  border: 1px solid #1f2937;
}

.prose-custom pre {
  background: #111827;
  border: 1px solid #1f2937;
  border-radius: 0.75rem;
  padding: 1.25rem 1.5rem;
  margin: 1.25rem 0;
  overflow-x: auto;
}

.prose-custom pre code {
  background: none;
  border: none;
  padding: 0;
  font-size: 0.875rem;
  color: #e5e7eb;
}

.prose-custom table {
  width: 100%;
  border-collapse: collapse;
  margin: 1.25rem 0;
  font-size: 0.875rem;
}

.prose-custom th {
  background: #1f2937;
  color: #f3f4f6;
  font-weight: 600;
  padding: 0.6rem 1rem;
  border: 1px solid #374151;
  text-align: left;
}

.prose-custom td {
  padding: 0.5rem 1rem;
  border: 1px solid #1f2937;
  color: #d1d5db;
}

.prose-custom tr:nth-child(even) td {
  background: #0f172a;
}

.prose-custom blockquote {
  border-left: 3px solid #22c55e;
  padding-left: 1rem;
  margin: 1rem 0;
  color: #9ca3af;
}

.prose-custom strong {
  color: #f3f4f6;
  font-weight: 600;
}

.prose-custom a {
  color: #4ade80;
  text-decoration: underline;
}

.prose-custom hr {
  border-color: #1f2937;
  margin: 2rem 0;
}
</style>
