<template>
  <button v-bind="$attrs" class="btn" @click="onClick">
    <i v-if="icon" :class="icon" />
    <slot>
      <span v-if="label">{{ label }}</span>
    </slot>
  </button>
</template>

<script setup lang="ts">
const props = defineProps<{
  icon?: string
  label?: string
}>()

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const onClick = (event: MouseEvent) => {
  emit('click', event)
}

defineOptions({
  name: 'GenesisButton',
})
</script>
