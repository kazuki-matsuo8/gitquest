import { defineStore } from 'pinia'

interface AuthState {
  token: string | null
  userId: string | null
  username: string | null
  email: string | null
}

interface LoginPayload {
  email: string
  password: string
}

interface RegisterPayload {
  username: string
  email: string
  password: string
}

interface AuthResponse {
  accessToken: string
  tokenType: string
  userId: string
  username: string
  email: string
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: null,
    userId: null,
    username: null,
    email: null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
  },

  actions: {
    // localStorage からトークンを復元（ページリロード時）
    init() {
      if (import.meta.client) {
        const token = localStorage.getItem('token')
        const userId = localStorage.getItem('userId')
        const username = localStorage.getItem('username')
        const email = localStorage.getItem('email')
        if (token) {
          this.token = token
          this.userId = userId
          this.username = username
          this.email = email
        }
      }
    },

    async login(payload: LoginPayload) {
      const config = useRuntimeConfig()
      const data = await $fetch<AuthResponse>(`${config.public.apiBase}/auth/login`, {
        method: 'POST',
        body: payload,
      })
      this._saveSession(data)
    },

    async register(payload: RegisterPayload) {
      const config = useRuntimeConfig()
      const data = await $fetch<AuthResponse>(`${config.public.apiBase}/auth/register`, {
        method: 'POST',
        body: payload,
      })
      this._saveSession(data)
    },

    logout() {
      this.token = null
      this.userId = null
      this.username = null
      this.email = null
      if (import.meta.client) {
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('email')
      }
    },

    _saveSession(data: AuthResponse) {
      this.token = data.accessToken
      this.userId = data.userId
      this.username = data.username
      this.email = data.email
      if (import.meta.client) {
        localStorage.setItem('token', data.accessToken)
        localStorage.setItem('userId', data.userId)
        localStorage.setItem('username', data.username)
        localStorage.setItem('email', data.email)
      }
    },
  },
})
