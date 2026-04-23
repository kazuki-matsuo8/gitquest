import { useAuthStore } from '~/stores/auth'

// ログインしていないユーザーを /login にリダイレクトするミドルウェア
export default defineNuxtRouteMiddleware(() => {
  const auth = useAuthStore()
  auth.init()
  if (!auth.isLoggedIn) {
    return navigateTo('/login')
  }
})
