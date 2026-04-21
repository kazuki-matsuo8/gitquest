<template>
  <main class="flex items-center justify-center min-h-[calc(100vh-4rem)] px-4 py-12">
    <div class="w-full max-w-md">
      <div class="bg-gray-900 border border-gray-800 rounded-2xl p-8">
        <h1 class="text-2xl font-bold text-center mb-2">新規登録</h1>
        <p class="text-gray-400 text-sm text-center mb-8">アカウントを作成して始めよう</p>

        <!-- エラー -->
        <div v-if="errorMessage" class="mb-6 bg-red-500/10 border border-red-500/30 text-red-400 text-sm rounded-xl px-4 py-3">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleRegister" class="flex flex-col gap-5">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">ユーザー名</label>
            <input
              v-model="form.username"
              type="text"
              required
              placeholder="gitmaster"
              class="w-full bg-gray-800 border border-gray-700 rounded-xl px-4 py-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
            />
          </div>
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
              minlength="8"
              placeholder="8文字以上"
              class="w-full bg-gray-800 border border-gray-700 rounded-xl px-4 py-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
            />
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-green-600 hover:bg-green-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-xl transition-colors mt-2"
          >
            {{ loading ? '登録中...' : 'アカウントを作成' }}
          </button>
        </form>

        <p class="text-center text-sm text-gray-400 mt-6">
          すでにアカウントをお持ちの方は
          <NuxtLink to="/login" class="text-green-400 hover:text-green-300 transition-colors">
            ログイン
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

const form = reactive({ username: '', email: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

async function handleRegister() {
  loading.value = true
  errorMessage.value = ''
  try {
    await auth.register(form)
    router.push('/missions')
  } catch {
    errorMessage.value = '登録に失敗しました。メールアドレスが既に使用されている可能性があります'
  } finally {
    loading.value = false
  }
}
</script>
