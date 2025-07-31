<template>
  <div class="d-flex" id="app-layout">
    <GenesisSidebar />
    <!-- Main content -->
    <div class="bg-light flex-grow-1 p-4">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import GenesisSidebar from "./core/navbar/GenesisSidebar.vue";
</script>

<style scoped></style>
