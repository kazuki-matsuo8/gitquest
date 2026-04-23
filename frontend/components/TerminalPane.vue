<template>
  <div class="flex flex-col h-full bg-gray-950 rounded-xl border border-gray-800 overflow-hidden font-mono text-sm">
    <!-- ターミナルのタイトルバー（信号機風） -->
    <div class="flex items-center gap-2 px-4 py-3 bg-gray-900 border-b border-gray-800 shrink-0">
      <span class="w-3 h-3 rounded-full bg-red-500" />
      <span class="w-3 h-3 rounded-full bg-yellow-500" />
      <span class="w-3 h-3 rounded-full bg-green-500" />
      <span class="ml-3 text-xs text-gray-400 tracking-wide">gitquest terminal</span>
    </div>

    <!-- 出力ログ -->
    <div
      ref="logEl"
      class="flex-1 overflow-y-auto px-4 py-3 space-y-1 min-h-0"
    >
      <div v-for="(entry, i) in log" :key="i">
        <!-- 入力コマンド行 -->
        <div v-if="entry.type === 'input'" class="flex gap-2">
          <span class="text-green-400 shrink-0">$</span>
          <span class="text-white">{{ entry.text }}</span>
        </div>
        <!-- 出力行 -->
        <div
          v-else
          class="whitespace-pre-wrap leading-relaxed"
          :class="entry.type === 'error' ? 'text-red-400' : 'text-gray-300'"
        >{{ entry.text }}</div>
      </div>

      <!-- カーソル点滅（入力待ち） -->
      <div v-if="!loading" class="flex gap-2 items-center">
        <span class="text-green-400">$</span>
        <span class="w-2 h-4 bg-green-400 animate-pulse" />
      </div>
      <div v-else class="text-gray-500 text-xs">実行中...</div>
    </div>

    <!-- コマンド入力エリア -->
    <div class="border-t border-gray-800 bg-gray-900 px-4 py-3 shrink-0">
      <form @submit.prevent="sendCommand" class="flex gap-2 items-center">
        <span class="text-green-400 shrink-0">$</span>
        <input
          ref="inputEl"
          v-model="input"
          type="text"
          :disabled="loading || !sessionId"
          placeholder="git init"
          class="flex-1 bg-transparent text-white placeholder-gray-600 focus:outline-none"
          @keydown.up.prevent="navigateHistory(-1)"
          @keydown.down.prevent="navigateHistory(1)"
        />
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  sessionId: string | null
}>()

const emit = defineEmits<{
  graphUpdated: [graph: unknown]
}>()

const config = useRuntimeConfig()

interface LogEntry {
  type: 'input' | 'output' | 'error'
  text: string
}

const log = ref<LogEntry[]>([
  { type: 'output', text: 'GitQuest ターミナルへようこそ！\ngit init からはじめてみよう。' },
])
const input = ref('')
const loading = ref(false)
const history = ref<string[]>([])
const historyIndex = ref(-1)
const logEl = ref<HTMLElement | null>(null)
const inputEl = ref<HTMLInputElement | null>(null)

async function sendCommand() {
  const cmd = input.value.trim()
  if (!cmd || !props.sessionId) return

  // 履歴に追加
  history.value.unshift(cmd)
  historyIndex.value = -1
  input.value = ''

  log.value.push({ type: 'input', text: cmd })
  loading.value = true

  try {
    const res = await $fetch<{ output: string; success: boolean; graph: unknown }>(
      `${config.public.apiBase}/terminal/sessions/${props.sessionId}/exec`,
      { method: 'POST', body: { command: cmd } }
    )
    log.value.push({ type: res.success ? 'output' : 'error', text: res.output })
    emit('graphUpdated', res.graph)
  } catch {
    log.value.push({ type: 'error', text: 'サーバーエラーが発生しました' })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
    inputEl.value?.focus()
  }
}

function navigateHistory(dir: -1 | 1) {
  const next = historyIndex.value + dir
  if (next < -1 || next >= history.value.length) return
  historyIndex.value = next
  input.value = next === -1 ? '' : history.value[next]
}

function scrollToBottom() {
  if (logEl.value) {
    logEl.value.scrollTop = logEl.value.scrollHeight
  }
}

onMounted(() => {
  inputEl.value?.focus()
})
</script>
