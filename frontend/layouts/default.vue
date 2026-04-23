<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <!-- ナビバー -->
    <nav class="border-b border-gray-800 bg-gray-900/80 backdrop-blur-sm sticky top-0 z-50">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <!-- ロゴ -->
          <NuxtLink to="/" class="flex items-center gap-2 text-green-400 hover:text-green-300 transition-colors">
            <span class="text-xl font-bold tracking-tight">GitQuest</span>
          </NuxtLink>

          <!-- ナビリンク -->
          <div class="flex items-center gap-2 sm:gap-4">
            <template v-if="auth.isLoggedIn">
              <NuxtLink
                to="/dashboard"
                class="text-sm text-gray-300 hover:text-white px-3 py-2 rounded-lg hover:bg-gray-800 transition-colors hidden sm:block"
              >
                ダッシュボード
              </NuxtLink>
              <NuxtLink
                to="/missions"
                class="text-sm text-gray-300 hover:text-white px-3 py-2 rounded-lg hover:bg-gray-800 transition-colors"
              >
                ミッション
              </NuxtLink>
              <button
                class="text-sm text-gray-400 hover:text-red-400 px-3 py-2 rounded-lg hover:bg-gray-800 transition-colors"
                @click="logout"
              >
                ログアウト
              </button>
            </template>
            <template v-else>
              <NuxtLink
                to="/login"
                class="text-sm text-gray-300 hover:text-white px-3 py-2 rounded-lg hover:bg-gray-800 transition-colors"
              >
                ログイン
              </NuxtLink>
              <NuxtLink
                to="/register"
                class="text-sm bg-green-600 hover:bg-green-500 text-white px-4 py-2 rounded-lg transition-colors"
              >
                新規登録
              </NuxtLink>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <!-- ページコンテンツ -->
    <slot />
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const auth = useAuthStore()
const router = useRouter()

onMounted(() => {
  auth.init()
})

function logout() {
  auth.logout()
  router.push('/')
}
</script>
