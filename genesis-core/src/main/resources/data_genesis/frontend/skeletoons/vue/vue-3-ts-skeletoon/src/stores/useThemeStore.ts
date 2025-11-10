import { defineStore } from 'pinia'
import { ref } from 'vue'
const themes: string[] = [
  'genesis-light',
  'genesis-dark',
  'cupcake',
  'bumblebee',
  'emerald',
  'corporate',
  'synthwave',
  'retro',
  'cyberpunk',
  'valentine',
  'halloween',
  'garden',
  'forest',
  'aqua',
  'lofi',
  'pastel',
  'fantasy',
  'wireframe',
  'black',
  'luxury',
  'dracula',
  'cmyk',
  'autumn',
  'business',
  'acid',
  'lemonade',
  'night',
  'coffee',
  'winter',
  'dim',
  'nord',
  'sunset',
  'caramellatte',
  'abyss',
  'silk',
]
export const useThemeStore = defineStore('theme', () => {
  const defaultTheme:string = themes[0]
  const storedTheme:string | null = localStorage.getItem('theme')

  const theme = ref<string>(storedTheme || defaultTheme)

  const applyTheme = (newTheme: string) => {
    if (themes.includes(newTheme)) {
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
  return { theme, applyTheme, resetTheme, availableThemes: themes, defaultTheme }
})
