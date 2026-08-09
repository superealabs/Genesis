<template>
  <button class="btn btn-outline btn-md" @click="toggleLayout">
    {{ $t('settings.switchLayout') }}
  </button>
</template>

<script setup lang="ts">
import { useLayoutStore } from '@/stores/useLayoutStore.ts'
const layoutStore = useLayoutStore()

const toggleLayout = () => {
  layoutStore.setLayout(layoutStore.currentLayout === 'sidebar' ? 'topbar' : 'sidebar')
}
</script>
