<template>
  <main>
    <!-- ヒーローセクション -->
    <section class="relative overflow-hidden">
      <div class="absolute inset-0 bg-linear-to-br from-gray-950 via-gray-900 to-gray-950 pointer-events-none" />
      <div class="absolute top-20 left-1/4 w-72 h-72 bg-green-500/10 rounded-full blur-3xl pointer-events-none" />
      <div class="absolute bottom-10 right-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

      <div class="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-16 lg:pt-24 pb-20 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
        <!-- 左: コピー -->
        <div class="text-center lg:text-left">
          <span class="inline-block text-xs font-semibold tracking-widest text-green-400 uppercase mb-4 bg-green-400/10 px-3 py-1 rounded-full">
            Git 学習プラットフォーム
          </span>
          <h1 class="text-4xl sm:text-5xl font-bold mb-6 leading-tight">
            本物のターミナルで、<br>
            <span class="text-green-400">Git</span> を体で覚える
          </h1>
          <p class="text-gray-400 text-lg max-w-xl mx-auto lg:mx-0 mb-8 leading-relaxed">
            コマンドを打つたびにコミットグラフがリアルタイムで動く。
            解説を読んで、実際に手を動かして、Git の仕組みを根本から理解しよう。
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
            <NuxtLink
              :to="auth.isLoggedIn ? '/missions' : '/register'"
              class="px-8 py-3.5 bg-green-600 hover:bg-green-500 text-white font-semibold rounded-xl transition-all hover:scale-105 active:scale-95 shadow-lg shadow-green-600/20"
            >
              {{ auth.isLoggedIn ? 'ミッションを続ける' : '無料で始める' }}
            </NuxtLink>
            <NuxtLink
              to="/missions"
              class="px-8 py-3.5 bg-gray-800 hover:bg-gray-700 text-gray-200 font-semibold rounded-xl transition-all hover:scale-105 active:scale-95"
            >
              ミッションを見る
            </NuxtLink>
          </div>
        </div>

        <!-- 右: ターミナル + グラフのデモ -->
        <div class="relative hidden sm:block">
          <!-- ターミナルモックアップ -->
          <div class="bg-gray-900 border border-gray-800 rounded-2xl overflow-hidden shadow-2xl shadow-black/50">
            <div class="flex items-center gap-1.5 px-4 py-3 bg-gray-800/60 border-b border-gray-800">
              <span class="w-3 h-3 rounded-full bg-red-500/80" />
              <span class="w-3 h-3 rounded-full bg-yellow-500/80" />
              <span class="w-3 h-3 rounded-full bg-green-500/80" />
              <span class="ml-3 text-xs text-gray-500 font-mono">gitquest — terminal</span>
            </div>
            <div class="p-5 font-mono text-sm leading-7">
              <p class="demo-line" style="animation-delay: .2s">
                <span class="text-green-400">~/project</span> $ git checkout -b feature
              </p>
              <p class="demo-line text-gray-400" style="animation-delay: .9s">
                Switched to a new branch 'feature'
              </p>
              <p class="demo-line" style="animation-delay: 1.6s">
                <span class="text-green-400">~/project</span> $ git commit -m "新機能を追加"
              </p>
              <p class="demo-line text-gray-400" style="animation-delay: 2.3s">
                [feature <span class="text-yellow-400">a3f2c1d</span>] 新機能を追加
              </p>
              <p class="demo-line" style="animation-delay: 3.0s">
                <span class="text-green-400">~/project</span> $ git merge feature
                <span class="cursor-blink text-gray-200">▍</span>
              </p>
            </div>
          </div>

          <!-- コミットグラフのミニデモ -->
          <div class="absolute -bottom-8 -right-4 lg:-right-8 bg-gray-900 border border-gray-800 rounded-2xl p-4 shadow-2xl shadow-black/50 w-44">
            <p class="text-[10px] text-gray-500 mb-2 font-semibold tracking-wide uppercase">Commit Graph</p>
            <svg viewBox="0 0 120 130" class="w-full">
              <!-- エッジ -->
              <path d="M30 110 V70" class="graph-edge" style="animation-delay: 3.4s" stroke="#4ade80" />
              <path d="M30 70 C30 50, 70 60, 70 40" class="graph-edge" style="animation-delay: 3.7s" stroke="#60a5fa" fill="none" />
              <path d="M30 70 V20" class="graph-edge" style="animation-delay: 4.0s" stroke="#4ade80" />
              <path d="M70 40 C70 25, 30 35, 30 20" class="graph-edge" style="animation-delay: 4.3s" stroke="#60a5fa" fill="none" />
              <!-- ノード -->
              <circle cx="30" cy="110" r="7" class="graph-node" style="animation-delay: 3.3s" fill="#030712" stroke="#4ade80" stroke-width="2.5" />
              <circle cx="30" cy="70" r="7" class="graph-node" style="animation-delay: 3.6s" fill="#030712" stroke="#4ade80" stroke-width="2.5" />
              <circle cx="70" cy="40" r="7" class="graph-node" style="animation-delay: 3.9s" fill="#030712" stroke="#60a5fa" stroke-width="2.5" />
              <circle cx="30" cy="20" r="7" class="graph-node graph-node-head" style="animation-delay: 4.5s" fill="#030712" stroke="#4ade80" stroke-width="2.5" />
            </svg>
          </div>
        </div>
      </div>
    </section>

    <!-- 数字バンド -->
    <section class="border-y border-gray-800 bg-gray-900/40">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 grid grid-cols-3 gap-6 text-center">
        <div>
          <p class="text-3xl sm:text-4xl font-bold text-green-400 mb-1">12</p>
          <p class="text-xs sm:text-sm text-gray-500">ミッション</p>
        </div>
        <div>
          <p class="text-3xl sm:text-4xl font-bold text-blue-400 mb-1">4</p>
          <p class="text-xs sm:text-sm text-gray-500">レベル (入門〜上級)</p>
        </div>
        <div>
          <p class="text-3xl sm:text-4xl font-bold text-yellow-400 mb-1">100%</p>
          <p class="text-xs sm:text-sm text-gray-500">本物の Git 環境</p>
        </div>
      </div>
    </section>

    <!-- 特徴セクション -->
    <section class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
      <h2 class="text-2xl sm:text-3xl font-bold text-center mb-4 text-gray-100">
        GitQuest が選ばれる理由
      </h2>
      <p class="text-gray-500 text-center mb-12 max-w-2xl mx-auto">
        「読むだけ」では Git は身につかない。実際に打って、壊して、直して覚える。
      </p>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="feature in features"
          :key="feature.title"
          class="bg-gray-900 border border-gray-800 rounded-2xl p-6 hover:border-green-500/50 hover:-translate-y-1 transition-all"
        >
          <div class="text-3xl mb-4">{{ feature.icon }}</div>
          <h3 class="font-semibold text-lg mb-2 text-gray-100">{{ feature.title }}</h3>
          <p class="text-gray-400 text-sm leading-relaxed">{{ feature.description }}</p>
        </div>
      </div>
    </section>

    <!-- 学習の流れ -->
    <section class="bg-gray-900/40 border-y border-gray-800">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <h2 class="text-2xl sm:text-3xl font-bold text-center mb-12 text-gray-100">学習の流れ</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div v-for="(step, i) in steps" :key="step.title" class="relative text-center md:text-left">
            <div class="flex items-center justify-center md:justify-start gap-4 mb-4">
              <span class="w-10 h-10 rounded-full bg-green-600/20 text-green-400 font-bold flex items-center justify-center text-lg shrink-0">
                {{ i + 1 }}
              </span>
              <h3 class="font-semibold text-lg text-gray-100">{{ step.title }}</h3>
            </div>
            <p class="text-gray-400 text-sm leading-relaxed">{{ step.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- レベル紹介 -->
    <section class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
      <h2 class="text-2xl sm:text-3xl font-bold text-center mb-12 text-gray-100">
        入門から上級まで、段階的に
      </h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="lv in levels"
          :key="lv.level"
          class="bg-gray-900 border border-gray-800 rounded-2xl p-6 flex flex-col gap-3"
        >
          <span :class="lv.badgeClass" class="self-start text-xs font-bold px-3 py-1 rounded-full">
            Lv.{{ lv.level }}
          </span>
          <h3 class="font-semibold text-gray-100">{{ lv.title }}</h3>
          <ul class="text-sm text-gray-400 flex flex-col gap-1.5">
            <li v-for="item in lv.items" :key="item" class="flex items-center gap-2">
              <span class="text-green-500 text-xs">✔</span>{{ item }}
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="relative overflow-hidden border-t border-gray-800">
      <div class="absolute inset-0 bg-linear-to-b from-gray-950 to-gray-900 pointer-events-none" />
      <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-160 h-64 bg-green-500/10 rounded-full blur-3xl pointer-events-none" />
      <div class="relative max-w-3xl mx-auto px-4 sm:px-6 py-20 text-center">
        <h2 class="text-2xl sm:text-3xl font-bold mb-4 text-gray-100">
          今日から Git を使いこなそう
        </h2>
        <p class="text-gray-400 mb-8">
          登録は 30 秒。ブラウザだけで、今すぐ最初のコミットを打てます。
        </p>
        <NuxtLink
          :to="auth.isLoggedIn ? '/missions' : '/register'"
          class="inline-block px-10 py-4 bg-green-600 hover:bg-green-500 text-white font-semibold rounded-xl transition-all hover:scale-105 active:scale-95 shadow-lg shadow-green-600/20"
        >
          {{ auth.isLoggedIn ? 'ミッション一覧へ' : '無料で始める' }}
        </NuxtLink>
      </div>
    </section>

    <!-- フッター -->
    <footer class="border-t border-gray-800 bg-gray-950">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex flex-col sm:flex-row items-center justify-between gap-4">
        <p class="text-sm text-gray-600">© 2026 GitQuest</p>
        <div class="flex items-center gap-6 text-sm text-gray-500">
          <NuxtLink to="/missions" class="hover:text-gray-300 transition-colors">ミッション</NuxtLink>
          <a
            href="https://github.com/kazuki-matsuo8/gitquest"
            target="_blank"
            rel="noopener"
            class="hover:text-gray-300 transition-colors"
          >
            GitHub
          </a>
        </div>
      </div>
    </footer>
  </main>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const auth = useAuthStore()

const features = [
  {
    icon: '🖥️',
    title: '本物のターミナル',
    description: '疑似環境ではなく PTY ベースの本物のシェル。vim も rebase -i もそのまま動く。',
  },
  {
    icon: '🌳',
    title: 'リアルタイムグラフ',
    description: 'コマンドを打つたびにコミットグラフが即座に更新。ブランチの分岐とマージが目で見てわかる。',
  },
  {
    icon: '📖',
    title: '解説 → 演習の二段構え',
    description: '各ミッションの前に概念をしっかり解説。理解してから手を動かすから定着する。',
  },
  {
    icon: '🏆',
    title: '進捗が見える',
    description: 'クリア状況・レベル別の達成率をダッシュボードで管理。学習のモチベーションが続く。',
  },
]

const steps = [
  {
    title: '解説を読む',
    description: 'ミッションを始める前に、その回で学ぶ Git の概念をやさしい言葉とイラストで解説。',
  },
  {
    title: 'ターミナルで実践',
    description: '本物の Git 環境でコマンドを実行。グラフの変化を見ながら仕組みを体感する。',
  },
  {
    title: '自動判定でクリア',
    description: 'リポジトリの状態をサーバーが自動チェック。条件を満たせばその場でクリア判定。',
  },
]

const levels = [
  {
    level: 1,
    title: '入門 — 基本操作',
    badgeClass: 'bg-green-500/15 text-green-400',
    items: ['git init', 'git add', 'git commit'],
  },
  {
    level: 2,
    title: '初級 — ブランチ操作',
    badgeClass: 'bg-blue-500/15 text-blue-400',
    items: ['git branch', 'git checkout', 'git merge'],
  },
  {
    level: 3,
    title: '中級 — 履歴・差分',
    badgeClass: 'bg-purple-500/15 text-purple-400',
    items: ['git log', 'git diff', 'git status'],
  },
  {
    level: 4,
    title: '上級 — 実践テクニック',
    badgeClass: 'bg-orange-500/15 text-orange-400',
    items: ['コンフリクト解消', 'git commit --amend', 'git revert'],
  },
]
</script>

<style scoped>
/* ターミナルの行が順番にフェードインする */
.demo-line {
  opacity: 0;
  animation: line-in 0.4s ease forwards;
}

@keyframes line-in {
  from { opacity: 0; transform: translateY(4px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* カーソル点滅 */
.cursor-blink {
  animation: blink 1.1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* グラフのエッジが描かれていく */
.graph-edge {
  stroke-width: 2.5;
  stroke-dasharray: 100;
  stroke-dashoffset: 100;
  animation: draw 0.5s ease forwards;
}

@keyframes draw {
  to { stroke-dashoffset: 0; }
}

/* ノードがポップして現れる */
.graph-node {
  opacity: 0;
  transform-origin: center;
  transform-box: fill-box;
  animation: pop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes pop {
  from { opacity: 0; transform: scale(0); }
  to   { opacity: 1; transform: scale(1); }
}

/* HEAD ノードは現れた後ゆっくり明滅する */
.graph-node-head {
  animation: pop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) forwards,
             pulse 2.4s ease-in-out 5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.55; }
}
</style>
