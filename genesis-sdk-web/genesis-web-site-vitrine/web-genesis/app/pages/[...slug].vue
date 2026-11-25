<script setup lang="ts">
// Toutes les pages du site (docs, ecosystem, about...) : contenu stocké dans la table « page »
const route = useRoute()
const path = computed(() => route.path.replace(/\/$/, '') || '/')
</script>

<template>
  <CmsPage :key="path" :route="path" />
</template>
