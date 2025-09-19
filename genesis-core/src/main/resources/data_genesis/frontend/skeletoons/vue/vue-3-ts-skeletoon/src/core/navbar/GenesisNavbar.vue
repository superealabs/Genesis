<template>
  <nav class="flex-1 flex flex-col">
    <ul class="menu flex-1 flex flex-col gap-2 overflow-hidden">
      <li v-for="section in navigations" :key="section.sectionName">
        <!-- Section title -->
        <span
          class="menu-title font-semibold text-neutral uppercase opacity-30 flex items-center gap-2"
        >
          <font-awesome-icon v-if="section.icon" :icon="section.icon" />
          {{ section.sectionName }}
        </span>
        <!-- Items -->
        <GenesisNavList :items="section.navChilds" class="" />
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useNavigationsStore } from '@/stores/useNavigationsStore'
import GenesisNavList from './GenesisNavList.vue'

const navigationsStore = useNavigationsStore()
const { navigations } = storeToRefs(navigationsStore)
</script>

<style scoped>
/* Plus besoin de position: fixed ici */
.menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
</style>
