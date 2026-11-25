// https://nuxt.com/docs/api/configuration/nuxt-config
import { fileURLToPath } from 'node:url'
import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  css: ['~/assets/css/site.css'],

  // Sans les commentaires HTML : Headless UI (menus genesis-vsc, as="template") n'accepte qu'un seul enfant
  vue: { compilerOptions: { comments: false } },

  // Composants rangés en sous-dossiers (playground/...) utilisables par leur nom de fichier
  components: [{ path: '~/components', pathPrefix: false }],

  app: {
    head: {
      htmlAttrs: { lang: 'fr' },
      title: 'Genesis · Votre base de données devient une application',
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'Genesis génère vos projets Spring Boot, .NET et frontend JS à partir de votre base de données.' },
        { name: 'theme-color', content: '#000f0f' }
      ],
      link: [
        { rel: 'icon', href: '/assets/img/genie-normal.svg', type: 'image/svg+xml' },
        { rel: 'preload', href: '/assets/fonts/Chillax-Variable.woff2', as: 'font', type: 'font/woff2', crossorigin: '' },
        { rel: 'preload', href: '/assets/fonts/Poppins-Variable.woff2', as: 'font', type: 'font/woff2', crossorigin: '' }
      ],
      // data-theme est géré par useHead (app.vue) à partir du cookie « genesis-theme ».
      // Ancien choix gardé seulement dans localStorage : on le recopie une fois dans le cookie.
      script: [
        { innerHTML: "try{if(!/(^|; )genesis-theme=/.test(document.cookie)){var t=localStorage.getItem('genesis-theme');if(t==='light'){document.cookie='genesis-theme=light;path=/;max-age=31536000;samesite=lax';document.documentElement.setAttribute('data-theme','light')}}}catch(e){}" }
      ]
    }
  },

  // Adresses des backends.
  //  - public.* : vues par le NAVIGATEUR. Toujours sur la même adresse que le site (pas de CORS) :
  //      en local, devProxy (plus bas) fait suivre ; en ligne, c'est la passerelle Nginx (deploy/).
  //  - *Internal : vues par le SERVEUR Nuxt (rendu des pages, génération du .zip).
  //      En local : appel direct ; sous Docker : NUXT_SITEBACK_INTERNAL / NUXT_GENESIS_API_INTERNAL.
  runtimeConfig: {
    sitebackInternal: 'http://localhost:9000/siteback',
    genesisApiInternal: 'http://127.0.0.1:8081/api',
    // Dossier partagé avec genesis-api pour les projets générés (sous Docker : NUXT_WORK_DIR=/work)
    workDir: '',
    public: {
      genesisApi: '/genesis-api/api',
      siteback: '/siteback',
      // « Écrire dans un dossier » du Playground : à désactiver en ligne (NUXT_PUBLIC_ALLOW_LOCAL_FOLDERS=false)
      allowLocalFolders: true
    }
  },

  // Composants et styles de genesis-vsc (genesis-web-core) utilisés par le Playground
  alias: {
    // Design system partagé avec genesis-vsc : utilisé directement depuis genesis-sdk-web/genesis-web-core
    // (plus de copie dans le site). Une correction dans genesis-web-core profite aux deux.
    '@genesis-labs/web-core': fileURLToPath(new URL('../../genesis-web-core/src', import.meta.url)),
    '@genesis-labs/shared-types': fileURLToPath(new URL('../../genesis-web-types-shared/src', import.meta.url))
  },

  vite: {
    plugins: [tailwindcss()],
    // pnpm dev : autoriser Vite à servir les fichiers de genesis-web-core (polices, images), hors du dossier du site
    server: { fs: { allow: [fileURLToPath(new URL('../..', import.meta.url))] } },
    resolve: {
      // genesis-web-core est hors du dossier du site : ses « import 'vue' » doivent utiliser
      // les paquets du site (node_modules de web-genesis), pas une deuxième copie.
      dedupe: ['vue', 'vue-router', 'pinia', '@headlessui/vue', 'gsap',
        '@codemirror/state', '@codemirror/view', '@codemirror/language', '@codemirror/commands',
        '@codemirror/lang-sql', '@codemirror/lang-json', '@lezer/highlight']
    },
    optimizeDeps: {
      include: ['@codemirror/commands', '@codemirror/lang-json', '@codemirror/lang-sql', '@codemirror/state', '@codemirror/view', '@headlessui/vue']
    }
  },

  // Fichiers envoyés (photos, charte, contenus HTML des pages) : public/upload/{images,icon,navbar}
  nitro: {
    storage: {
      uploads: { driver: 'fs', base: './public/upload' }
    },
    // Développement (pnpm dev) : le navigateur appelle /siteback et /genesis-api sur le port du site,
    // Nuxt fait suivre vers les vrais backends (comme Nginx en ligne). Aucun CORS nécessaire.
    devProxy: {
      '/siteback': { target: 'http://localhost:9000/siteback', changeOrigin: true },
      '/genesis-api': { target: 'http://127.0.0.1:8081', changeOrigin: true }
    }
  }
})
