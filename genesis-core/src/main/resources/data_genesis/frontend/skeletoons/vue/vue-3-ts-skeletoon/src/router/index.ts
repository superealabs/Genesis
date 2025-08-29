import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
import { useFreezeScreen } from '@/stores/useFreezeScreen'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  const loading = useFreezeScreen()
  loading.freeze('Loading')
  next()
})

router.afterEach(() => {
  const loading = useFreezeScreen()
  setTimeout(() => loading.unfreeze(), 150) // small delay to prevent flicker
})

export default router
