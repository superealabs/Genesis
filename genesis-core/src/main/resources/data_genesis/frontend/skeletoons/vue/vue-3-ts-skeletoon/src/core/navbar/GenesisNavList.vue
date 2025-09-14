<template>
  <ul class="flex flex-col gap-1">
    <li v-for="item in items" :key="item.navTitle">
      <!-- Lien actif -->
      <router-link
        v-if="item.navLink"
        :to="item.navLink"
        class="block px-3 py-2 rounded-md transition-colors"
      >
        {{ item.navTitle }}
      </router-link>

      <!-- Pas de lien -->
      <span v-else class="block px-3 py-2 text-gray-400">
        {{ item.navTitle }}
      </span>

      <!-- Enfants -->
      <GenesisNavList v-if="item.navChilds" :items="item.navChilds" class="ml-4" />
    </li>
  </ul>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'

interface NavItem {
  navTitle: string
  navLink?: string
  navChilds?: NavItem[]
}

defineProps<{
  items: NavItem[]
}>()
</script>

<style scoped>
.nav-link.active {
  font-weight: bold;
  color: var(--color-primary);
}

.nav-link-action:hover,
.nav-link-action:focus,
.router-link-active {
  color: var(--color-primary);
  background-color: var(--color-secondary);
  border-radius: 0.25rem;
  transition: all 0.2s ease-in-out;
}

.router-link-active {
  position: relative;
  font-weight: bold;
}

.router-link-active::before {
  background-color: var(--color-primary);
  content: '';
  position: absolute;
  top: 0;
  left: -7px;
  height: 100%;
  width: 5px;
  border-radius: 10px;
}
</style>
