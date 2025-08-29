<template>
  <div class="flex gap-2">
    <select v-model="theme" @change="applyTheme" class="select select-bordered">
      <option v-for="t in themes" :key="t" :value="t">
        {{ t }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const themes = ['genesis-light', 'genesis-dark']
const theme = ref('genesis-light')

const applyTheme = () => {
  document.documentElement.setAttribute('data-theme', theme.value)
  localStorage.setItem('theme', theme.value) // sauvegarde
}

onMounted(() => {
  const saved = localStorage.getItem('theme')
  if (saved && themes.includes(saved)) {
    theme.value = saved
    applyTheme()
  }
})
</script>
