<template>
  <div class="flex flex-wrap items-center justify-between py-2 gap-2">
    <!-- Pagination -->
    <nav class="flex items-center gap-2 flex-wrap join">
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
        <span v-else class="text-gray-400 px-2"> ... </span>
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

    <!-- Quick form -->
    <div v-if="quickForm" class="flex items-center gap-2">
      <label for="select-page" class="whitespace-nowrap text font-medium">
        {{ $t('pagination.goToLabel') }}
      </label>
      <input
        class="input"
        type="number"
        v-model="selectedPage"
        min="1"
        :max="end"
        id="select-page"
      />
      <span class="text-nowrap">/ {{ end }}</span>
      <GenesisButton class="btn btn-outline border-0 shadow" @click="goToSelectedPage">
        {{ $t('button.go') }}
      </GenesisButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { SelectOption } from '@/models/SelectOption'
import GenesisButton from '@/core/button/GenesisButton.vue'
import CheveronLeftIcon from '../icons/CheveronLeftIcon.vue'
import CheveronRightIcon from '../icons/CheveronRightIcon.vue'

// Props definition using defineProps (Composition API)
const props = defineProps<{
  start: number
  end: number
  page: number
  quickForm?: boolean
}>()

// Emits using defineEmits (Composition API)
const emit = defineEmits<{
  (e: 'update:page', value: number): void
}>()

const selectedPage = ref<number>(1)

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

const pageSelectOptions = computed(() => {
  const totalPages = props.end
  const options: SelectOption[] = []
  for (let index = 1; index <= totalPages; index++) {
    options.push({
      label: index.toString(),
      value: index,
    })
  }
  return options
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

function goToSelectedPage() {
  onChangePage(selectedPage.value)
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
