<template>
  <ul class="menu bg-base-200 rounded-box w-full overflow-hidden">
    <li v-for="item in items" :key="item.navTitle" class="">
      <!-- Item with link -->
      <router-link
        v-if="item.navLink"
        :to="item.navLink"
        class="block px-3 py-2 rounded-md transition-colors hover:bg-base-300"
      >
        {{ item.navTitle }}
      </router-link>

      <!-- Nested items -->
      <details
        v-if="item.navChilds"
        class="ml-2 mt-1"
        :open="item.navChilds?.some((child) => child.navLink === $route.path)"
      >
        <summary class="cursor-pointer px-3 py-2 rounded-md hover:bg-base-300">
          {{ item.navTitle }}
        </summary>
        <GenesisNavList :items="item.navChilds" />
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

const props = defineProps<{
  items: NavItem[]
}>()

const $route = useRoute()
</script>

<style scoped>
/* Active link styling */
.router-link-active {
  font-weight: bold;
  color: var(--color-primary);
  position: relative;
}

.router-link-active::before {
  content: '';
  position: absolute;
  top: 0;
  left: -7px;
  width: 5px;
  height: 100%;
  background-color: var(--color-primary);
  border-radius: 10px;
}
</style>
