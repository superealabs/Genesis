<script setup lang="ts">
import { ref } from 'vue'
import SortIcon from '@/components/icons/SortIcon.vue'
import { SortFieldParameter } from '@/models/api/RequestModel.ts'

const props = defineProps<{
  fieldKey: string
}>()

const emit = defineEmits<{
  (e: 'sort', sortData: SortFieldParameter): void
}>()

const currentSort = ref(false)
function sort() {
  emit('sort', new SortFieldParameter(props.fieldKey, undefined, !currentSort.value))
  currentSort.value = !currentSort.value
}
</script>

<template>
  <div class="flex flex-col justify-center items-center">
    <!-- Ascending -->
    <button
      class="text-primary-content/80 hover:text-primary-content cursor-pointer transition-colors duration-200"
      @click="sort"
      title="Sort ascending"
      type="button"
    >
      <SortIcon />
    </button>
  </div>
</template>

<style scoped></style>
