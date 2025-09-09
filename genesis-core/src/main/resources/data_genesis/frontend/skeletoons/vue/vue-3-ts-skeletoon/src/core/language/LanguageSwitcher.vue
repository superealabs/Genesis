<template>
  <div class="flex items-center gap-2">
    <label for="lang-select" class="font-medium text-neutral">Language:</label>
    <select id="lang-select" v-model="locale" class="select select-bordered">
      <option v-for="locale in availableLocales" :key="`locale-${locale}`" :value="locale">
        {{ locale }}
      </option>
    </select>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useI18n } from 'vue-i18n'

export default defineComponent({
  name: 'LanguageSwitcher',
  setup() {
    const { locale, availableLocales } = useI18n({ useScope: 'global' })
    return { locale, availableLocales }
  },
})
</script>

<style scoped>
.select {
  min-width: 120px;
}
</style>
