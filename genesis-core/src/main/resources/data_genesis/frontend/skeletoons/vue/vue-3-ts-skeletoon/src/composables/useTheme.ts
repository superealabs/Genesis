import { ref, onMounted } from 'vue'

const theme = ref<'theme-light' | 'theme-dark'>('theme-light')

function setTheme(newTheme: 'theme-light' | 'theme-dark') {
  theme.value = newTheme
  document.body.classList.remove('theme-light', 'theme-dark')
  document.body.classList.add(newTheme)
  localStorage.setItem('theme', newTheme)
}

onMounted(() => {
  const saved = localStorage.getItem('theme') as 'theme-light' | 'theme-dark' | null
  if (saved) setTheme(saved)
  else setTheme('theme-light')
})

export function useTheme() {
  return {
    theme,
    setTheme,
  }
}
