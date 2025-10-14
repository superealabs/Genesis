<script setup lang="ts">
import GenesisButton from '@/components/button/GenesisButton.vue'
import { ref } from 'vue'
defineProps<{
  end: number
}>()

const emit = defineEmits<{
  (e: 'change:page', page: number): void
}>()

const selectedPage = ref<number>(1)
function goToSelectedPage() {
  emit('change:page', selectedPage.value)
}
</script>

<template>
  <div class="flex items-center gap-2 text-sm">
    <label for="select-page" class="whitespace-nowrap font-medium">
      {{ $t('pagination.goToLabel') }}
    </label>
    <input
      id="select-page"
      type="number"
      class="input input-bordered w-20 h-8"
      v-model="selectedPage"
      min="1"
      :max="end"
      @keydown.enter="goToSelectedPage"
    />
    <GenesisButton class="btn btn-outline border-0 shadow h-8 px-3" @click="goToSelectedPage">
      {{ $t('button.go') }}
    </GenesisButton>
  </div>
</template>

<style scoped></style>
