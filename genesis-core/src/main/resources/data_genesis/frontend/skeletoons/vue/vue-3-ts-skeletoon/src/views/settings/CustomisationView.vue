<template>
  <div class="fluid-container">
    <h3>Customisation View</h3>
    <div>
      <label for="themeSwitcher">Choose theme:</label>
      <ThemeSwitcher />
      <LanguageSwitcher />
    </div>
  </div>
</template>

<script lang="ts">
import LanguageSwitcher from '@/core/language/LanguageSwitcher.vue'
import ThemeSwitcher from '@/core/theme/ThemeSwitcher.vue'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'CustomisationView',
  components: { ThemeSwitcher, LanguageSwitcher },
  setup() {
    return {}
  },
})
</script>
