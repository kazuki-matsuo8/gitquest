<template>
  <div class="min-h-screen bg-gray-950 text-white">
    <!-- ナビバー -->
    <nav class="border-b border-gray-800 bg-gray-900/80 backdrop-blur-sm sticky top-0 z-50">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <!-- ロゴ -->
          <NuxtLink to="/" class="flex items-center gap-2.5 text-green-400 hover:text-green-300 transition-colors">
            <svg viewBox="0 0 32 32" class="w-7 h-7" aria-hidden="true">
              <rect width="32" height="32" rx="7" class="fill-gray-900" />
              <path d="M10 11v10" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
              <path d="M22 15.2c0 3.8-4.6 3.6-8.6 5.4" stroke="currentColor" stroke-width="2.2" fill="none" stroke-linecap="round" />
              <circle cx="10" cy="8" r="3" class="fill-gray-900" stroke="currentColor" stroke-width="2.2" />
              <circle cx="10" cy="24" r="3" class="fill-gray-900" stroke="currentColor" stroke-width="2.2" />
              <circle cx="22" cy="12" r="3" class="fill-gray-900" stroke="currentColor" stroke-width="2.2" />
            </svg>
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
