<template>
  <button class="btn btn-outline btn-sm" @click="toggleLayout">
    Switch to {{ layoutStore.currentLayout === 'sidebar' ? 'Topbar' : 'Sidebar' }} Layout
  </button>
</template>

<script setup lang="ts">
import { useLayoutStore } from '@/stores/useLayoutStore.ts'
const layoutStore = useLayoutStore()

const toggleLayout = () => {
  layoutStore.setLayout(layoutStore.currentLayout === 'sidebar' ? 'topbar' : 'sidebar')
}
</script>
