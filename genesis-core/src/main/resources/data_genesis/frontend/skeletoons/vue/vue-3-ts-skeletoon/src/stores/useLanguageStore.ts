import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

export const useLanguageStore = defineStore('language', () => {
  const i18n = useI18n()
  const storedLanguage = localStorage.getItem('language')
  const language = ref<string>(storedLanguage || i18n.locale.value) // en | fr | mg ...

  const setLanguage = (newLang: string) => {
    language.value = newLang
    i18n.locale.value = newLang
    localStorage.setItem('language', newLang)
  }
  setLanguage(language.value)

  return {
    language,
    setLanguage,
  }
})
