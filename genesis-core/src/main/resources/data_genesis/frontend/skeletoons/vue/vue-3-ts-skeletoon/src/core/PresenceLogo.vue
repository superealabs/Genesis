<template>
  <LogoIcon :src="logo" alt="Branding Logo" :width="size" />
</template>

<script setup lang="ts">
import LogoIcon from '@/core/icons/LogoIcon.vue'
import logo from '@/assets/images/logo.png'

defineProps({
  size: {
    type: String,
    default: "60",
  },
})
</script>
