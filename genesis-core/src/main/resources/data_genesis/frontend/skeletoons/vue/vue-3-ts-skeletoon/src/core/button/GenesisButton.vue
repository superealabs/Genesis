<template>
  <button v-bind="$attrs" class="btn" @click="onClick">
    <i v-if="icon" :class="icon" />
    <slot>
      <span v-if="label">{{ label }}</span>
    </slot>
  </button>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'GenesisButton',
  props: {
    icon: { type: String, required: false }, // icône optionnelle
    label: { type: String, required: false },
  },
  emits: {
    click: (event: MouseEvent) => event instanceof MouseEvent,
  },
  setup(props, { emit }) {
    const onClick = (event: MouseEvent) => {
      emit('click', event)
    }
    return {
      onClick,
    }
  },
})
</script>
