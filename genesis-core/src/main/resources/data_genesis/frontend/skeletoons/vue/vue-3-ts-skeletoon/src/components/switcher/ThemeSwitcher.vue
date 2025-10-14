<template>
  <div class="flex gap-2">
    <select v-model="themeStore.theme" @change="applyTheme" class="select bottom-0">
      <option v-for="t in themeStore.availableThemes" :key="t" :value="t">
        {{ t }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/useThemeStore'

const themeStore = useThemeStore()

// Apply theme to <html data-theme="">
const applyTheme = () => {
  themeStore.applyTheme(themeStore.theme)
}
</script>
