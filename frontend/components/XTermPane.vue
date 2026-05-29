<template>
  <div class="flex flex-col h-full bg-gray-950">
    <!-- ターミナル本体 -->
    <div ref="termContainer" class="flex-1 overflow-hidden p-1" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { Terminal } from '@xterm/xterm'
import { FitAddon } from '@xterm/addon-fit'
import '@xterm/xterm/css/xterm.css'

const props = defineProps<{
  sessionId: string
  apiBase: string
  onGraphUpdate?: () => void
}>()

const termContainer = ref<HTMLDivElement>()
let term: Terminal | null = null
let fitAddon: FitAddon | null = null
let ws: WebSocket | null = null
let resizeObserver: ResizeObserver | null = null

// ────────────────────────────────────────────
// xterm.js 初期化
// ────────────────────────────────────────────

function initTerminal() {
  if (!termContainer.value) return

  term = new Terminal({
    theme: {
      background:  '#030712',   // gray-950
      foreground:  '#e5e7eb',   // gray-200
      cursor:      '#22c55e',   // green-500
      black:       '#1f2937',
      red:         '#ef4444',
      green:       '#22c55e',
      yellow:      '#eab308',
      blue:        '#3b82f6',
      magenta:     '#a855f7',
      cyan:        '#06b6d4',
      white:       '#f3f4f6',
      brightBlack: '#374151',
      brightRed:   '#f87171',
      brightGreen: '#4ade80',
      brightYellow:'#facc15',
      brightBlue:  '#60a5fa',
      brightMagenta:'#c084fc',
      brightCyan:  '#22d3ee',
      brightWhite: '#ffffff',
    },
    fontFamily: "'JetBrains Mono', 'Fira Code', 'Cascadia Code', monospace",
    fontSize: 14,
    lineHeight: 1.4,
    cursorBlink: true,
    scrollback: 3000,
    allowProposedApi: true,
  })

  fitAddon = new FitAddon()
  term.loadAddon(fitAddon)
  term.open(termContainer.value)
  fitAddon.fit()

  // OSC 9999 ハンドラ: PS1 が描画されるたびにグラフを更新
  term.parser.registerOscHandler(9999, () => {
    props.onGraphUpdate?.()
    return true
  })

  // キー入力を WebSocket に送信
  term.onData((data) => {
    if (ws?.readyState === WebSocket.OPEN) {
      const bytes = new TextEncoder().encode(data)
      ws.send(bytes)
    }
  })

  // バイナリ入力 (特殊キーなど) を WebSocket に送信
  term.onBinary((data) => {
    if (ws?.readyState === WebSocket.OPEN) {
      const bytes = Uint8Array.from(data, c => c.charCodeAt(0))
      ws.send(bytes)
    }
  })

  // コンテナのリサイズを監視して PTY にも通知
  resizeObserver = new ResizeObserver(() => {
    fitAddon?.fit()
    sendResize()
  })
  resizeObserver.observe(termContainer.value)
}

// ────────────────────────────────────────────
// WebSocket 接続
// ────────────────────────────────────────────

function connectWebSocket() {
  // http → ws, https → wss に変換
  const wsBase = props.apiBase.replace(/^http/, 'ws')
  ws = new WebSocket(`${wsBase}/ws/terminal/${props.sessionId}`)
  ws.binaryType = 'arraybuffer'

  ws.onopen = () => {
    // 接続直後にウィンドウサイズを送信
    sendResize()
  }

  ws.onmessage = (event) => {
    if (!term) return
    if (event.data instanceof ArrayBuffer) {
      term.write(new Uint8Array(event.data))
    } else {
      term.write(event.data)
    }
  }

  ws.onclose = () => {
    term?.writeln('\r\n\x1b[33m[セッション終了]\x1b[0m')
  }

  ws.onerror = () => {
    term?.writeln('\r\n\x1b[31m[接続エラー]\x1b[0m')
  }
}

// PTY にウィンドウサイズ変更を通知
function sendResize() {
  if (!term || !ws || ws.readyState !== WebSocket.OPEN) return
  const msg = JSON.stringify({ type: 'resize', cols: term.cols, rows: term.rows })
  ws.send(msg)
}

// ────────────────────────────────────────────
// ライフサイクル
// ────────────────────────────────────────────

onMounted(() => {
  initTerminal()
  connectWebSocket()
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  ws?.close()
  term?.dispose()
})

// sessionId が変わったら再接続
watch(() => props.sessionId, () => {
  ws?.close()
  term?.clear()
  connectWebSocket()
})
</script>
