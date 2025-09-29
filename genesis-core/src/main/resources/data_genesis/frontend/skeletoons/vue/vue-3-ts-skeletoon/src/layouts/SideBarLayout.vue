<template>
  <div>
    <GenesisBanner :height="topbarHeight" />
    <div
      class="w-full grid fixed top-0 left-0"
      :style="{ gridTemplateColumns: `auto 1fr`, marginTop: topbarHeight }"
    >
      <!-- Sidebar -->
      <aside
        id="navbar"
        class="bg-base-100 shadow-sm flex-col overflow-y-scroll"
        :style="{ height: `calc(100vh - ${topbarHeight})`, width: sidebarWidth }"
      >
        <GenesisNavbar />
      </aside>

      <!-- Main content -->
      <main class="overflow-auto p-4" :style="{ height: `calc(100vh - ${topbarHeight})` }">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import GenesisBanner from '@/core/navbar/GenesisBanner.vue'
import GenesisNavbar from '@/core/navbar/GenesisNavbar.vue'

defineProps({
  topbarHeight: { type: String, default: '56px' },
  sidebarWidth: { type: String, default: '300px' },
})
</script>

<style scoped></style>
