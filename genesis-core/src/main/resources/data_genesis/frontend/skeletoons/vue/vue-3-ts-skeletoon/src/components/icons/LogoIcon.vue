<template>
  <component
    :is="link ? 'a' : 'span'"
    :href="link"
    :target="link ? target : undefined"
    rel="noopener noreferrer"
    class="inline-block"
  >
    <img
      :src="src"
      :alt="alt || 'Logo'"
      :width="width"
      :height="height"
      class="object-contain text-base-content"
    />
  </component>
</template>
<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  src: string // logo path or URL
  alt?: string // alt text for the logo
  link?: string // optional hyperlink
  width?: string // optional width (default: auto)
  height?: string // optional height (default: auto)
  newTab?: boolean // open link in new tab (default: false)
}>()

const target = computed(() => (props.newTab ? '_blank' : '_self'))
</script>

<style scoped>
img {
  display: block;
}
</style>
