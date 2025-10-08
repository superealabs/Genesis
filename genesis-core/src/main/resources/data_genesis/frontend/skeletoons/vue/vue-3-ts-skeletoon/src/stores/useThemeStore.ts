import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const availableThemes = ['genesis-light', 'genesis-dark', 'valentine', 'retro']
  const defaultTheme = availableThemes[0]
  const storedTheme = localStorage.getItem('theme')

  const theme = ref<string>(storedTheme || defaultTheme) // genesis-light | genesis-dark | valentine | retro

  const applyTheme = (newTheme: string) => {
    if (availableThemes.includes(newTheme)) {
      theme.value = newTheme
      localStorage.setItem('theme', newTheme)
      document.documentElement.setAttribute('data-theme', theme.value)
    } else {
      console.warn(`Theme "${newTheme}" is not available.`)
    }
  }

  const resetTheme = () => {
    applyTheme(defaultTheme)
  }
  applyTheme(theme.value)
  return { theme, applyTheme, resetTheme, availableThemes, defaultTheme }
})
