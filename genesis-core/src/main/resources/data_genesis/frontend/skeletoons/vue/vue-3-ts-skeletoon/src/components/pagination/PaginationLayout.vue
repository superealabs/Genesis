<template>
  <div class="flex flex-wrap items-center justify-between py-2 gap-3">
    <!-- Total info -->
    <PageDataRange
      :start-element="startElement"
      :end-element="endElement"
      :total-elements="totalElements"
    />

    <!-- Pagination controls -->
    <nav class="flex items-center gap-1 flex-wrap join">
      <!-- Previous -->
      <button
        type="button"
        class="btn btn-ghost rounded shadow mr-2"
        :class="{ 'btn-disabled': page <= 1 }"
        :aria-label="$t('pagination.previous')"
        @click.prevent="goToPreviousPage"
      >
        <CheveronLeftIcon />
      </button>

      <!-- Pages -->
      <template v-for="pageNum in pageNumbers" :key="pageNum">
        <button
          v-if="pageNum !== '...'"
          class="btn rounded join-item"
          :class="pageNum === page ? 'btn-primary' : 'btn-ghost'"
          @click.prevent="onChangePage(pageNum)"
        >
          {{ pageNum }}
        </button>
        <span v-else class="text-gray-400 px-2">…</span>
      </template>

      <!-- Next -->
      <button
        type="button"
        class="btn btn-ghost rounded shadow ml-2"
        :class="{ 'btn-disabled': page >= end }"
        :aria-label="$t('pagination.next')"
        @click.prevent="goToNextPage"
      >
        <CheveronRightIcon />
      </button>
    </nav>

    <!-- Quick jump -->
    <PagingationForm v-show="quickForm" :end="end" @change:page="onChangePage" />
  </div>
</template>

<script setup lang="ts">
import CheveronLeftIcon from '../icons/CheveronLeftIcon.vue'
import CheveronRightIcon from '../icons/CheveronRightIcon.vue'
import PageDataRange from '@/components/pagination/PageDataRange.vue'
import PagingationForm from '@/components/pagination/PagingationForm.vue'
import { computed } from 'vue'

const props = defineProps<{
  start: number
  end: number
  page: number
  totalElements?: number
  startElement?: number
  endElement?: number
  quickForm?: boolean
}>()

// Emits using defineEmits (Composition API)
const emit = defineEmits<{
  (e: 'update:page', value: number): void
}>()

const pageNumbers = computed(() => {
  const pages: (number | string)[] = []
  const maxVisible = 10 // show 10 pages at a time
  const totalPages = props.end
  const currentPage = props.page

  // Figure out the current "block" of pages
  const currentBlock = Math.floor((currentPage - 1) / maxVisible)
  const startPage = currentBlock * maxVisible + 1
  const endPage = Math.min(startPage + maxVisible - 1, totalPages)

  // Add first page + ellipsis if we are not in the first block
  if (startPage > 1) {
    pages.push(1)
    if (startPage > 2) {
      pages.push('...')
    }
  }

  // Add the block of pages
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i)
  }

  // Add ellipsis + last page if not in the last block
  if (endPage < totalPages) {
    if (endPage < totalPages - 1) {
      pages.push('...')
    }
    pages.push(totalPages)
  }

  return pages
})

function onChangePage(page: number | string) {
  if (!page) {
    page = 1
  }
  if (typeof page == 'string') {
    page = Number.parseInt(page)
  }
  emit('update:page', page)
}

function goToPreviousPage() {
  if (props.page > 1) {
    emit('update:page', props.page - 1)
  }
}

function goToNextPage() {
  if (props.page < props.end) {
    emit('update:page', props.page + 1)
  }
}
</script>

<style scoped></style>
