<template>
  <ul class="menu w-auto overflow-hidden">
    <li v-for="item in items" :key="item.navTitle">
      <!-- Item with link -->
      <router-link
        v-if="item.navLink" :to="item.navLink" class="menu-title rounded-md nav-item"
      >
        {{ $t(item.navTitle) }}
      </router-link>

      <!-- Nested items -->
      <details
        v-if="item.navChilds"
        class=""
        :open="item.navChilds?.some((child) => child.navLink === $route.path)"
      >
        <summary class="rounded-md py-0 nav-item">
          <span class="menu-title">{{ $t(item.navTitle) }}</span>
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

defineProps<{
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
  background-color: var(--color-base-100);
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
