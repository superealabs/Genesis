<template>
  <div>
    <h1>Welcome on DEMO frontend app. {{ t('greet') }} Developer</h1>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useI18n } from 'vue-i18n'

export default defineComponent({
  name: 'HomeView',
  setup() {
    const { t } = useI18n({ useScope: 'global' })

    return { t }
  },
})
</script>

<style scoped></style>
