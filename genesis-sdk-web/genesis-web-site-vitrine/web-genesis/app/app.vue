<script setup lang="ts">
// Thème appliqué sur <html data-theme="..."> depuis l'état (cookie) : rendu serveur et navigateur identiques
const { theme } = useTheme()
useHead({ htmlAttrs: { 'data-theme': theme } })
</script>

<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>