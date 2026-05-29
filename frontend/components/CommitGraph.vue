<template>
  <div class="flex flex-col h-full bg-gray-950 rounded-xl border border-gray-800 overflow-hidden">
    <!-- タイトルバー -->
    <div class="px-4 py-3 bg-gray-900 border-b border-gray-800 shrink-0 flex items-center justify-between gap-3 min-w-0">
      <span class="text-xs text-gray-400 tracking-wide font-mono shrink-0">commit graph</span>
      <!-- ブランチ凡例 -->
      <div v-if="branchLegend.length > 0" class="flex items-center gap-3 flex-wrap justify-end min-w-0">
        <div v-for="item in branchLegend" :key="item.name" class="flex items-center gap-1.5">
          <span class="w-2 h-2 rounded-full shrink-0" :style="{ background: item.color }" />
          <span class="text-xs text-gray-400 font-mono truncate max-w-20">{{ item.name }}</span>
        </div>
      </div>
    </div>

    <!-- グラフ本体 -->
    <div class="flex-1 overflow-auto">
      <!-- コミットなし -->
      <div
        v-if="commits.length === 0"
        class="flex flex-col items-center justify-center h-full gap-3 text-gray-600 p-8 text-center"
      >
        <div class="w-12 h-12 rounded-full border-2 border-dashed border-gray-800 flex items-center justify-center">
          <span class="w-2 h-2 rounded-full bg-gray-700 block" />
        </div>
        <p class="text-sm leading-relaxed">
          git commit するとグラフが表示されます
        </p>
      </div>

      <!-- SVG グラフ -->
      <div v-else class="overflow-x-auto p-3">
        <svg
          :width="svgWidth"
          :height="svgHeight"
          class="block overflow-visible"
        >
          <!-- エッジ (ノードより先に描画してノードで隠れるように) -->
          <path
            v-for="edge in edges"
            :key="`${edge.from}-${edge.to}`"
            :d="edge.d"
            :stroke="edge.color"
            fill="none"
            stroke-width="1.5"
            stroke-opacity="0.65"
          />

          <!-- コミットノード -->
          <g
            v-for="node in layoutNodes"
            :key="node.hash"
            class="cursor-pointer select-none"
            @click="toggleSelected(node.hash)"
          >
            <!-- クリック領域 (タッチ操作のため少し大きめ) -->
            <circle :cx="node.x" :cy="node.y" r="14" fill="transparent" />

            <!-- コミット円 -->
            <circle
              :cx="node.x"
              :cy="node.y"
              :r="NODE_R"
              :fill="node.color"
              :stroke="selectedHash === node.hash ? '#fff' : node.color"
              :stroke-width="selectedHash === node.hash ? 2.5 : 0"
              :fill-opacity="0.95"
            />

            <!-- ブランチラベル (テキスト行と同じ Y に横並び) -->
            <g
              v-for="(branch, bi) in node.branches.slice(0, 3)"
              :key="branch.name"
            >
              <rect
                :x="node.labelXPositions[bi]"
                :y="node.y - 7"
                :width="branchLabelW(branch.name)"
                height="14"
                rx="3"
                :fill="branch.isHead ? '#15803d' : node.color"
                fill-opacity="0.9"
              />
              <text
                :x="node.labelXPositions[bi] + 4"
                :y="node.y + 4"
                fill="white"
                font-size="9"
                font-family="monospace"
              >{{ branch.name }}</text>
            </g>

            <!-- 短縮ハッシュ -->
            <text
              :x="textX"
              :y="node.y + 4"
              fill="#6b7280"
              font-size="11"
              font-family="monospace"
            >{{ node.shortHash }}</text>

            <!-- コミットメッセージ -->
            <text
              :x="textX + HASH_W"
              :y="node.y + 4"
              fill="#d1d5db"
              font-size="12"
            >{{ truncate(node.message, MSG_CHARS) }}</text>
          </g>
        </svg>

        <!-- 選択コミットの詳細 -->
        <div
          v-if="selectedNode"
          class="mt-3 bg-gray-900 border border-gray-700 rounded-xl p-4 font-mono text-xs space-y-1.5"
        >
          <div class="text-green-400 break-all">commit {{ selectedNode.hash }}</div>
          <div class="text-gray-400">Author: {{ selectedNode.author }}</div>
          <div class="text-gray-400">Date:&nbsp;&nbsp; {{ selectedNode.timestamp }}</div>
          <div class="text-white mt-2 whitespace-pre-wrap wrap-break-word">{{ selectedNode.message }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { GraphData } from '~/types/terminal'

const props = defineProps<{
  graph: GraphData | null
}>()

// ブランチごとの色 (レーン 0 が青=メイン、以降は別色)
const LANE_COLORS = ['#3b82f6', '#f59e0b', '#ec4899', '#8b5cf6', '#06b6d4', '#f97316']

// レイアウト定数
const NODE_R = 7
const ROW_H = 44
const LANE_W = 20
const PADDING_X = 14
const PADDING_Y = 28
const TEXT_GAP = 14
const HASH_W = 62  // 短縮ハッシュ (7文字) の幅
const MSG_CHARS = 26
const MSG_W = MSG_CHARS * 7.2

const commits = computed(() => props.graph?.commits ?? [])
const branches = computed(() => props.graph?.branches ?? [])
const head = computed(() => props.graph?.head ?? '')

// コミットハッシュ → ブランチ情報マップ
const branchMap = computed(() => {
  const map = new Map<string, Array<{ name: string; isHead: boolean }>>()
  for (const b of branches.value) {
    const list = map.get(b.hash) ?? []
    list.push({ name: b.name, isHead: b.isHead })
    map.set(b.hash, list)
  }
  return map
})

// レーン割り当てアルゴリズム
// slots[i] = そのレーンが次に待っているコミットハッシュ (null = 空き)
const laneAssignment = computed((): Map<string, number> => {
  const laneOf = new Map<string, number>()
  if (commits.value.length === 0) return laneOf

  const slots: (string | null)[] = [commits.value[0].hash]

  for (const commit of commits.value) {
    let lane = slots.indexOf(commit.hash)

    if (lane === -1) {
      // どのレーンも待っていないコミット → 空きレーンか新規レーンを使う
      const freeIdx = slots.indexOf(null)
      if (freeIdx >= 0) {
        lane = freeIdx
      } else {
        lane = slots.length
        slots.push(null)
      }
    }

    laneOf.set(commit.hash, lane)

    if (commit.parents.length === 0) {
      // ルートコミット → レーンを解放
      slots[lane] = null
    } else {
      const firstParent = commit.parents[0]
      // 他のレーンがすでに first parent を待っていれば、このレーンを解放
      const alreadyTracked = slots.some((h, i) => i !== lane && h === firstParent)
      slots[lane] = alreadyTracked ? null : firstParent

      // マージコミットの追加親 → 新規レーンを割り当て
      for (let i = 1; i < commit.parents.length; i++) {
        const p = commit.parents[i]
        if (!slots.includes(p)) {
          const freeIdx = slots.indexOf(null)
          if (freeIdx >= 0) slots[freeIdx] = p
          else slots.push(p)
        }
      }
    }
  }

  return laneOf
})

const maxLanes = computed(() => {
  let max = 1
  for (const lane of laneAssignment.value.values()) {
    if (lane + 1 > max) max = lane + 1
  }
  return max
})

const laneColor = (lane: number) => LANE_COLORS[lane % LANE_COLORS.length]
const laneX = (lane: number) => PADDING_X + lane * LANE_W + LANE_W / 2
const rowY = (row: number) => PADDING_Y + row * ROW_H

// テキスト開始 X (レーン列の右端 + ギャップ)
const textX = computed(() => PADDING_X + maxLanes.value * LANE_W + TEXT_GAP)

// SVG サイズ
const svgWidth = computed(() => Math.max(480, textX.value + HASH_W + MSG_W + 120))
const svgHeight = computed(() => Math.max(80, PADDING_Y * 2 + commits.value.length * ROW_H))

function branchLabelW(name: string): number {
  return name.length * 6.5 + 10
}

// レイアウト済みノード (ラベル X 座標を事前計算)
const layoutNodes = computed(() =>
  commits.value.map((c, i) => {
    const lane = laneAssignment.value.get(c.hash) ?? 0
    const branchList = branchMap.value.get(c.hash) ?? []

    // ラベルをテキスト列の末尾に横並び配置
    let labelX = textX.value + HASH_W + MSG_W + 8
    const labelXPositions = branchList.slice(0, 3).map((b) => {
      const x = labelX
      labelX += branchLabelW(b.name) + 4
      return x
    })

    return {
      ...c,
      x: laneX(lane),
      y: rowY(i),
      lane,
      color: laneColor(lane),
      isHead: c.hash === head.value,
      branches: branchList,
      labelXPositions,
    }
  })
)

// コミット間のエッジ (Bezier 曲線)
const edges = computed(() => {
  const nodeMap = new Map(layoutNodes.value.map((n) => [n.hash, n]))
  const result: Array<{ from: string; to: string; d: string; color: string }> = []

  for (const n of layoutNodes.value) {
    for (let pi = 0; pi < n.parents.length; pi++) {
      const parent = nodeMap.get(n.parents[pi])
      if (!parent) continue

      const x1 = n.x
      const y1 = n.y
      const x2 = parent.x
      const y2 = parent.y
      const midY = (y1 + y2) / 2

      const d =
        x1 === x2
          ? `M ${x1} ${y1} L ${x2} ${y2}`
          : `M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}`

      // first parent はこのコミットの色、マージ親は親の色
      const color = pi === 0 ? n.color : parent.color
      result.push({ from: n.hash, to: n.parents[pi], d, color })
    }
  }
  return result
})

// タイトルバー用ブランチ凡例
const branchLegend = computed(() =>
  branches.value.map((b) => ({
    name: b.name,
    color: b.isHead ? '#22c55e' : laneColor(laneAssignment.value.get(b.hash) ?? 0),
  }))
)

// 選択状態
const selectedHash = ref<string | null>(null)
function toggleSelected(hash: string) {
  selectedHash.value = selectedHash.value === hash ? null : hash
}
const selectedNode = computed(() =>
  selectedHash.value
    ? layoutNodes.value.find((n) => n.hash === selectedHash.value) ?? null
    : null
)

function truncate(str: string, len: number): string {
  return str.length > len ? str.slice(0, len) + '…' : str
}
</script>
