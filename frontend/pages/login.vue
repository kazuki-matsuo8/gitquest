<template>
  <main class="flex items-center justify-center min-h-[calc(100vh-4rem)] px-4 py-12">
    <div class="w-full max-w-md">
      <!-- カード -->
      <div class="bg-gray-900 border border-gray-800 rounded-2xl p-8">
        <h1 class="text-2xl font-bold text-center mb-2">ログイン</h1>
        <p class="text-gray-400 text-sm text-center mb-8">GitQuest へようこそ</p>

        <!-- エラー -->
        <div v-if="errorMessage" class="mb-6 bg-red-500/10 border border-red-500/30 text-red-400 text-sm rounded-xl px-4 py-3">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleLogin" class="flex flex-col gap-5">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">メールアドレス</label>
            <input
              v-model="form.email"
              type="email"
              required
              placeholder="you@example.com"
              class="w-full bg-gray-800 border border-gray-700 rounded-xl px-4 py-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">パスワード</label>
            <input
              v-model="form.password"
              type="password"
              required
              placeholder="••••••••"
              class="w-full bg-gray-800 border border-gray-700 rounded-xl px-4 py-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
            />
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-green-600 hover:bg-green-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-xl transition-colors mt-2"
          >
            {{ loading ? 'ログイン中...' : 'ログイン' }}
          </button>
        </form>

        <p class="text-center text-sm text-gray-400 mt-6">
          アカウントをお持ちでない方は
          <NuxtLink to="/register" class="text-green-400 hover:text-green-300 transition-colors">
            新規登録
          </NuxtLink>
        </p>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ middleware: [] })

const auth = useAuthStore()
const router = useRouter()

const form = reactive({ email: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

async function handleLogin() {
  loading.value = true
  errorMessage.value = ''
  try {
    await auth.login(form)
    router.push('/missions')
  } catch {
    errorMessage.value = 'メールアドレスまたはパスワードが正しくありません'
  } finally {
    loading.value = false
  }
}
</script>
