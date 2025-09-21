<template>
  <div class="fluid-container">
    <h3>Customisation View</h3>
    <div class="flex gap-2 items-center">
      <label for="themeSwitcher">Choose theme:</label>
      <ThemeSwitcher />
      <LanguageSwitcher />
    </div>
  </div>
</template>

<script setup lang="ts">
import ThemeSwitcher from '@/core/theme/ThemeSwitcher.vue'
import LanguageSwitcher from '@/core/language/LanguageSwitcher.vue'
</script>
