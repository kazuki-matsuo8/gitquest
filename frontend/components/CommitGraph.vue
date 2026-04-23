<template>
  <div class="flex flex-col h-full bg-gray-950 rounded-xl border border-gray-800 overflow-hidden">
    <!-- タイトルバー -->
    <div class="px-4 py-3 bg-gray-900 border-b border-gray-800 shrink-0">
      <span class="text-xs text-gray-400 tracking-wide font-mono">commit graph</span>
    </div>

    <!-- グラフ本体 -->
    <div class="flex-1 overflow-auto p-4">
      <!-- コミットがない場合 -->
      <div v-if="commits.length === 0" class="flex flex-col items-center justify-center h-full gap-3 text-gray-600">
        <svg class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1"
            d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
        </svg>
        <p class="text-sm">git commit するとグラフが表示されます</p>
      </div>

      <!-- SVG グラフ -->
      <svg
        v-else
        :width="svgWidth"
        :height="svgHeight"
        class="overflow-visible"
      >
        <!-- エッジ（コミット間の接続線） -->
        <g v-for="edge in edges" :key="`${edge.from}-${edge.to}`">
          <line
            :x1="edge.x1" :y1="edge.y1"
            :x2="edge.x2" :y2="edge.y2"
            :stroke="edge.color"
            stroke-width="2"
            stroke-opacity="0.6"
          />
        </g>

        <!-- コミットノード -->
        <g
          v-for="node in nodes"
          :key="node.hash"
          class="cursor-pointer"
          @click="selectedHash = selectedHash === node.hash ? null : node.hash"
        >
          <!-- コミット円 -->
          <circle
            :cx="node.x" :cy="node.y" r="10"
            :fill="node.isHead ? '#22c55e' : '#3b82f6'"
            :stroke="selectedHash === node.hash ? '#fff' : 'transparent'"
            stroke-width="2"
          />
          <!-- 短縮ハッシュ -->
          <text
            :x="node.x + 16" :y="node.y + 4"
            fill="#9ca3af"
            font-size="11"
            font-family="monospace"
          >{{ node.shortHash }}</text>
          <!-- コミットメッセージ -->
          <text
            :x="node.x + 70" :y="node.y + 4"
            fill="#e5e7eb"
            font-size="12"
          >{{ truncate(node.message, 28) }}</text>

          <!-- ブランチラベル -->
          <g v-for="(branch, bi) in node.branches" :key="branch">
            <rect
              :x="node.x + 16" :y="node.y - 20 - bi * 18"
              :width="branch.length * 7 + 10" height="16"
              rx="4"
              :fill="branch === 'HEAD' ? '#16a34a' : '#1d4ed8'"
              fill-opacity="0.8"
            />
            <text
              :x="node.x + 21" :y="node.y - 8 - bi * 18"
              fill="white"
              font-size="10"
              font-family="monospace"
            >{{ branch }}</text>
          </g>
        </g>
      </svg>

      <!-- 選択したコミットの詳細 -->
      <div
        v-if="selectedNode"
        class="mt-4 bg-gray-900 border border-gray-700 rounded-xl p-4 font-mono text-sm space-y-1"
      >
        <div class="text-green-400">commit {{ selectedNode.hash }}</div>
        <div class="text-gray-400">Author: {{ selectedNode.author }}</div>
        <div class="text-gray-400">Date:   {{ selectedNode.timestamp }}</div>
        <div class="text-white mt-2">{{ selectedNode.message }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface CommitNode {
  hash: string
  shortHash: string
  message: string
  author: string
  timestamp: string
  parents: string[]
}

interface BranchRef {
  name: string
  hash: string
  isHead: boolean
}

interface GraphData {
  commits: CommitNode[]
  branches: BranchRef[]
  head: string
}

const props = defineProps<{
  graph: GraphData | null
}>()

const selectedHash = ref<string | null>(null)

const commits = computed(() => props.graph?.commits ?? [])
const branches = computed(() => props.graph?.branches ?? [])
const head = computed(() => props.graph?.head ?? '')

// コミットごとのブランチ名マップを構築
const branchMap = computed(() => {
  const map = new Map<string, string[]>()
  for (const b of branches.value) {
    const list = map.get(b.hash) ?? []
    if (b.isHead) list.unshift('HEAD')
    else list.push(b.name)
    map.set(b.hash, list)
  }
  return map
})

// SVG レイアウト定数
const ROW_H = 60
const COL_W = 40
const PADDING_X = 20
const PADDING_Y = 40

// コミットを新しい順（上）から配置
const nodes = computed(() => {
  return commits.value.map((c, i) => ({
    ...c,
    x: PADDING_X + COL_W,
    y: PADDING_Y + i * ROW_H,
    isHead: c.hash === head.value,
    branches: branchMap.value.get(c.hash) ?? [],
  }))
})

// 親子関係をエッジに変換
const edges = computed(() => {
  const nodeMap = new Map(nodes.value.map((n) => [n.hash, n]))
  const result = []
  for (const n of nodes.value) {
    for (const parentHash of n.parents) {
      const parent = nodeMap.get(parentHash)
      if (parent) {
        result.push({
          from: n.hash,
          to: parentHash,
          x1: n.x, y1: n.y,
          x2: parent.x, y2: parent.y,
          color: '#3b82f6',
        })
      }
    }
  }
  return result
})

const svgWidth = computed(() => 480)
const svgHeight = computed(() => Math.max(120, PADDING_Y * 2 + commits.value.length * ROW_H))

const selectedNode = computed(() =>
  selectedHash.value ? nodes.value.find((n) => n.hash === selectedHash.value) ?? null : null
)

function truncate(str: string, len: number): string {
  return str.length > len ? str.slice(0, len) + '…' : str
}
</script>
