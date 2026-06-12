import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2025-01-01',
  devtools: { enabled: true },

  app: {
    head: {
      htmlAttrs: { lang: 'ja' },
      title: 'GitQuest — 手を動かして学ぶ Git 学習プラットフォーム',
      meta: [
        { name: 'description', content: 'ブラウザ上の本物のターミナルで Git コマンドを実行しながら、コミットグラフがリアルタイムに動く。ミッションをクリアして Git をマスターしよう。' },
        { name: 'theme-color', content: '#030712' },
        { property: 'og:title', content: 'GitQuest — 手を動かして学ぶ Git 学習プラットフォーム' },
        { property: 'og:type', content: 'website' },
      ],
      link: [
        { rel: 'icon', type: 'image/svg+xml', href: '/favicon.svg' },
      ],
    },
  },

  modules: [
    '@pinia/nuxt',
    '@nuxt/eslint',
  ],

  css: ['~/assets/css/main.css'],

  vite: {
    plugins: [tailwindcss()],
  },

  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8085/api',
    },
  },

  typescript: {
    strict: true,
  },
})
