<template>
  <div class="w-full h-screen flex">
    <GenesisSideNavbar />
    <!-- Main content -->
    <main class="flex-1 h-full overflow-y-auto p-6 bg-base-100">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import GenesisSideNavbar from '@/components/navbar/GenesisSideNavbar.vue'
</script>
