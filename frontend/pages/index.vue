<template>
  <main>
    <!-- ヒーローセクション -->
    <section class="relative overflow-hidden">
      <!-- 背景のグラデーション -->
      <div class="absolute inset-0 bg-gradient-to-br from-gray-950 via-gray-900 to-gray-950 pointer-events-none" />
      <div class="absolute top-20 left-1/4 w-72 h-72 bg-green-500/10 rounded-full blur-3xl pointer-events-none" />
      <div class="absolute bottom-10 right-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

      <div class="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-24 text-center">
        <span class="inline-block text-xs font-semibold tracking-widest text-green-400 uppercase mb-4 bg-green-400/10 px-3 py-1 rounded-full">
          Git 学習プラットフォーム
        </span>
        <h1 class="text-4xl sm:text-5xl lg:text-6xl font-bold mb-6 leading-tight">
          コマンドを打って、
          <span class="text-green-400">Git</span>
          を学ぼう
        </h1>
        <p class="text-gray-400 text-lg sm:text-xl max-w-2xl mx-auto mb-10">
          ブラウザ上で Git コマンドを実行しながら、コミットグラフがリアルタイムで動く。
          ミッションをクリアして Git マスターを目指そう。
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <NuxtLink
            to="/register"
            class="px-8 py-3 bg-green-600 hover:bg-green-500 text-white font-semibold rounded-xl transition-all hover:scale-105 active:scale-95"
          >
            無料で始める
          </NuxtLink>
          <NuxtLink
            to="/missions"
            class="px-8 py-3 bg-gray-800 hover:bg-gray-700 text-gray-200 font-semibold rounded-xl transition-all hover:scale-105 active:scale-95"
          >
            ミッションを見る
          </NuxtLink>
        </div>
      </div>
    </section>

    <!-- 特徴セクション -->
    <section class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
      <h2 class="text-2xl sm:text-3xl font-bold text-center mb-12 text-gray-100">
        GitQuest でできること
      </h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="feature in features"
          :key="feature.title"
          class="bg-gray-900 border border-gray-800 rounded-2xl p-6 hover:border-green-500/50 transition-colors"
        >
          <div class="text-3xl mb-4">{{ feature.icon }}</div>
          <h3 class="font-semibold text-lg mb-2 text-gray-100">{{ feature.title }}</h3>
          <p class="text-gray-400 text-sm leading-relaxed">{{ feature.description }}</p>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
const features = [
  {
    icon: '💻',
    title: '擬似ターミナル',
    description: 'ブラウザ上で git コマンドを入力し、即座にフィードバックが返ってくる。',
  },
  {
    icon: '🌳',
    title: 'コミットグラフ',
    description: 'コマンドを打つたびにコミット履歴がリアルタイムで可視化される。',
  },
  {
    icon: '🎯',
    title: 'ミッション学習',
    description: '段階的なミッションをクリアしながら、実践的な Git スキルを身につける。',
  },
]
</script>
