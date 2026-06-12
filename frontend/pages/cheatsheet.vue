<template>
  <main class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <!-- ヘッダー -->
    <div class="mb-8 text-center">
      <h1 class="text-3xl font-bold mb-3">Git チートシート</h1>
      <p class="text-gray-400 text-sm">
        GitQuest で学ぶコマンドの早見表。困ったらここに戻ってこよう。
      </p>
    </div>

    <!-- 検索 -->
    <div class="mb-10 max-w-md mx-auto">
      <div class="relative">
        <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 text-sm">🔍</span>
        <input
          v-model="query"
          type="text"
          placeholder="コマンドやキーワードで検索 (例: branch)"
          class="w-full bg-gray-900 border border-gray-800 rounded-xl pl-10 pr-4 py-3 text-sm text-gray-200 placeholder-gray-600 focus:outline-none focus:border-green-500/50 transition-colors"
        >
      </div>
    </div>

    <!-- カテゴリごとのコマンド -->
    <div v-if="filteredCategories.length > 0" class="flex flex-col gap-10">
      <section v-for="cat in filteredCategories" :key="cat.title">
        <div class="flex items-center gap-3 mb-4">
          <span class="text-2xl">{{ cat.icon }}</span>
          <h2 class="text-lg font-semibold text-gray-200">{{ cat.title }}</h2>
          <div class="flex-1 h-px bg-gray-800" />
        </div>
        <div class="flex flex-col gap-2">
          <div
            v-for="cmd in cat.commands"
            :key="cmd.command"
            class="bg-gray-900 border border-gray-800 rounded-xl px-5 py-4 flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6 hover:border-gray-700 transition-colors"
          >
            <code class="font-mono text-sm text-green-400 sm:w-72 shrink-0 break-all">{{ cmd.command }}</code>
            <p class="text-sm text-gray-400 leading-relaxed">{{ cmd.description }}</p>
          </div>
        </div>
      </section>
    </div>

    <!-- 検索結果なし -->
    <div v-else class="text-center py-16">
      <p class="text-gray-500">「{{ query }}」に一致するコマンドが見つかりませんでした</p>
    </div>

    <!-- フッター CTA -->
    <div class="mt-16 text-center border-t border-gray-800 pt-10">
      <p class="text-gray-400 text-sm mb-4">読むだけじゃなく、実際に打って覚えよう</p>
      <NuxtLink
        to="/missions"
        class="inline-block px-8 py-3 bg-green-600 hover:bg-green-500 text-white font-semibold rounded-xl transition-colors"
      >
        ミッションで練習する
      </NuxtLink>
    </div>
  </main>
</template>

<script setup lang="ts">
useHead({ title: 'Git チートシート' })

interface Command {
  command: string
  description: string
}

interface Category {
  icon: string
  title: string
  commands: Command[]
}

const query = ref('')

const categories: Category[] = [
  {
    icon: '🚀',
    title: 'はじめる',
    commands: [
      { command: 'git init', description: 'このフォルダを Git リポジトリとして初期化する' },
      { command: 'git clone <URL>', description: 'リモートリポジトリを手元に複製する' },
      { command: 'git status', description: '現在の状態 (変更・ステージ済み・未追跡) を確認する' },
    ],
  },
  {
    icon: '📦',
    title: '記録する',
    commands: [
      { command: 'git add <ファイル>', description: '指定したファイルをステージングエリアに追加する' },
      { command: 'git add .', description: 'すべての変更をステージングエリアに追加する' },
      { command: 'git commit -m "メッセージ"', description: 'ステージした変更をコミットとして記録する' },
      { command: 'git commit --amend', description: '直前のコミットを作り直す (メッセージ修正・ファイル追加忘れ)' },
    ],
  },
  {
    icon: '🌿',
    title: 'ブランチ',
    commands: [
      { command: 'git branch', description: 'ブランチの一覧を表示する (* が現在地)' },
      { command: 'git branch <名前>', description: '新しいブランチを作成する' },
      { command: 'git checkout <名前>', description: '指定したブランチに切り替える' },
      { command: 'git checkout -b <名前>', description: 'ブランチの作成と切り替えを同時に行う' },
      { command: 'git switch <名前>', description: 'ブランチを切り替える (新しい書き方)' },
      { command: 'git merge <名前>', description: '指定したブランチの変更を今のブランチに取り込む' },
      { command: 'git branch -d <名前>', description: 'マージ済みのブランチを削除する' },
    ],
  },
  {
    icon: '🔍',
    title: '調べる',
    commands: [
      { command: 'git log', description: 'コミット履歴を表示する' },
      { command: 'git log --oneline', description: '履歴を 1 行ずつ簡潔に表示する' },
      { command: 'git log --graph --all', description: 'ブランチのグラフ付きで全履歴を表示する' },
      { command: 'git diff', description: 'ステージ前の変更内容 (差分) を表示する' },
      { command: 'git diff --staged', description: 'ステージ済みの変更内容を表示する' },
      { command: 'git show <ハッシュ>', description: '特定のコミットの内容を表示する' },
    ],
  },
  {
    icon: '⏪',
    title: '取り消す',
    commands: [
      { command: 'git revert <ハッシュ>', description: 'コミットを打ち消す新しいコミットを作る (履歴が残る・安全)' },
      { command: 'git restore <ファイル>', description: 'ファイルの変更を最後のコミットの状態に戻す' },
      { command: 'git restore --staged <ファイル>', description: 'ステージを取り消す (ファイルの変更は残る)' },
      { command: 'git reset --hard HEAD', description: 'すべての変更を破棄して最後のコミットに戻す (危険・要注意)' },
    ],
  },
  {
    icon: '☁️',
    title: 'リモート連携',
    commands: [
      { command: 'git remote add origin <URL>', description: 'リモートリポジトリを origin として登録する' },
      { command: 'git push origin <ブランチ>', description: 'ローカルのコミットをリモートに送る' },
      { command: 'git pull', description: 'リモートの変更を取得して取り込む' },
      { command: 'git fetch', description: 'リモートの変更を取得する (取り込みはしない)' },
    ],
  },
  {
    icon: '⚔️',
    title: 'コンフリクト対応',
    commands: [
      { command: 'git merge <名前> → 競合発生', description: '<<<<<<< と >>>>>>> のマーカーがファイルに入る' },
      { command: '(ファイルを編集して解決)', description: 'マーカーを消して残したい内容に書き換える' },
      { command: 'git add . && git commit', description: '解決した内容をコミットしてマージを完了する' },
      { command: 'git merge --abort', description: 'マージを中止して元の状態に戻る' },
    ],
  },
]

const filteredCategories = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return categories
  return categories
    .map((cat) => ({
      ...cat,
      commands: cat.commands.filter(
        (c) =>
          c.command.toLowerCase().includes(q)
          || c.description.toLowerCase().includes(q)
          || cat.title.toLowerCase().includes(q),
      ),
    }))
    .filter((cat) => cat.commands.length > 0)
})
</script>
