<template>
  <div class="flex flex-col w-full h-screen">
    <!-- Top Navigation -->
    <GenesisTopNavbar />

    <!-- Main content -->
    <main class="flex-1 overflow-y-auto bg-base-100 p-6">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import GenesisTopNavbar from '@/components/navbar/GenesisTopNavbar.vue'
</script>
