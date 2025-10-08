// stores/usePreferencesStore.ts
import { defineStore } from 'pinia'
import { useLanguageStore } from '@/stores/useLanguageStore.ts'
import { useThemeStore } from '@/stores/useThemeStore.ts'
export const useGlobalStore = defineStore('global', () => {
  useLanguageStore()
  useThemeStore()
  return {}
})
