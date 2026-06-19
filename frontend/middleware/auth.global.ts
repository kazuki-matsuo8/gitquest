import { useAuthStore } from '~/stores/auth'

// 認証が不要な公開ページ
const PUBLIC_PAGES = ['/', '/login', '/register']

// グローバル認証ガード
// 公開ページ以外はすべてログイン必須。未認証なら /login へ送る。
export default defineNuxtRouteMiddleware((to) => {
  // 認証情報は localStorage に保持しているため、SSR では判定できない。
  // クライアント側でのみ判定してリダイレクトのチラつきを防ぐ。
  if (import.meta.server) return

  const auth = useAuthStore()
  auth.init()

  const isPublic = PUBLIC_PAGES.includes(to.path)

  // 未ログインで保護ページにアクセス → ログイン画面へ（戻り先を記憶）
  if (!auth.isLoggedIn && !isPublic) {
    return navigateTo(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }

  // ログイン済みでログイン・登録画面に来たらダッシュボードへ
  if (auth.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    return navigateTo('/dashboard')
  }
})
