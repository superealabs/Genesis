<template>
  <ul class="menu w-full">
    <li v-for="item in items" :key="item.navTitle">
      <router-link
        v-if="item.navLink"
        :to="item.navLink"
        class="menu-title opacity-80 hover:opacity-100 transition"
        @click="closeAllDropdowns"
      >
        {{ $t(item.navTitle) }}
      </router-link>

      <details
        v-if="item.navChilds"
        :open="item.navChilds?.some((child) => child.navLink === $route.path)"
      >
        <summary class="opacity-80 hover:opacity-100">
          {{ $t(item.navTitle) }}
        </summary>
        <GenesisTopNavList :items="item.navChilds" @close="closeAllDropdowns" />
      </details>
    </li>
  </ul>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

interface NavItem {
  navTitle: string
  navLink?: string
  navChilds?: NavItem[]
}

defineProps<{
  items: NavItem[]
}>()

// 1. Définir l'événement "close"
const emit = defineEmits(['close'])

const $route = useRoute()

// 2. Fonction pour émettre l'événement de fermeture
const closeAllDropdowns = () => {
  emit('close')
}
</script>

<style scoped>
.router-link-active {
  font-weight: bold;
  color: var(--color-primary);
  position: relative;
}

.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--color-primary);
  border-radius: 4px;
}
</style>
