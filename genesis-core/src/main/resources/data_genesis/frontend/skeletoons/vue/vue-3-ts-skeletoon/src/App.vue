<template>
  <!-- FreezeScreen -->
  <teleport to="#freeze-screen">
    <FreezeScreen />
  </teleport>
  <div class="md:flex flex-col md:flex-row md:min-h-screen w-full">
    <!-- Sidebar -->
    <aside class="w-2/12 h-screen overflow-y-auto">
      <GenesisSidebar />
    </aside>

    <!-- Main content -->
    <main class="flex-1 overflow-y-auto bg-base-100 p-4">
      <router-view />
    </main>
  </div>
</template>

<script lang="ts">
import GenesisSidebar from './core/navbar/GenesisSidebar.vue'
import FreezeScreen from './core/loading/FreezeScreen.vue'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'App',
  components: { FreezeScreen, GenesisSidebar },
})
</script>

<style scoped>
/* Si tu veux garder un no-wrap explicite */
</style>
