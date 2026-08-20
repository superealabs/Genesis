<template>
  <div class="join border border-base-300 rounded-lg">
    <button
      class="join-item btn btn-sm"
      :class="{ 'btn-primary': modelValue === 'list' }"
      @click="$emit('update:modelValue', 'list')"
      title="Vue Liste"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-4 w-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 6h16M4 10h16M4 14h16M4 18h16"
        />
      </svg>
    </button>
    <button
      class="join-item btn btn-sm"
      :class="{ 'btn-primary': modelValue === 'card' }"
      @click="$emit('update:modelValue', 'card')"
      title="Vue Carte"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-4 w-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"
        />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps({
  modelValue: {
    type: String as () => 'list' | 'card',
    required: true,
  }
})

defineEmits<{
  (e: 'update:modelValue', value: 'list' | 'card'): void
}>()
</script>