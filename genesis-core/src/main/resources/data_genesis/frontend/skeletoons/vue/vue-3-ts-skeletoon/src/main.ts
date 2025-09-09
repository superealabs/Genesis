import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import '@/assets/styles/css/index.css'

/* import the fontawesome core */
import { library } from '@fortawesome/fontawesome-svg-core'
/* import font awesome icon component */
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
/* import all the icons in Free Solid, Duotone Solid, and Duotone Thin styles */
import { fas } from '@fortawesome/free-solid-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons'

library.add(fas, far, fab)
const i18n = createI18n({
  legacy: false,
  locale: 'en-US', // default local parameter
  fallbackLocale: 'en-US', // when no translation exist
  messages: {
    'en-US': {
      greet: 'Hello',
    },
    'es-ES': {
      greet: 'Hola',
    },
    'ja-JP': {
      greet: 'こんにちは、世界',
    },
  },
})
const pinia = createPinia()
const app = createApp(App)

app.component('font-awesome-icon', FontAwesomeIcon)
app.use(i18n)
app.use(router)
app.use(pinia)
app.mount('#app')
