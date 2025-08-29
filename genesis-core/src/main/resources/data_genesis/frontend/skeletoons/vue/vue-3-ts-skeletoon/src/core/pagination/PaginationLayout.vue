<template>
  <div class="flex flex-wrap items-center justify-between py-2 gap-2">
    <!-- Pagination -->
    <nav class="flex items-center gap-2 flex-wrap join">
      <!-- Previous -->
      <button
        type="button"
        class="btn btn-ghost rounded shadow mr-2"
        :class="{ 'btn-disabled': page <= 1 }"
        aria-label="Previous"
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
        aria-label="Next"
        @click.prevent="goToNextPage"
      >
        <CheveronRightIcon />
      </button>
    </nav>

    <!-- Quick form -->
    <div v-if="quickForm" class="flex items-center gap-2">
      <label for="select-page" class="whitespace-nowrap text font-medium"> Going to page: </label>
      <select id="select-page" v-model="selectedPage" class="select select-bordered">
        <option v-for="option in pageSelectOptions" :key="option.value" :value="option.value">
          {{ option.label }}
        </option>
      </select>
      <GenesisButton class="btn btn-outline border-0 shadow" @click="goToSelectedPage">
        GO
      </GenesisButton>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
import type { SelectOption } from '@/models/SelectOption'
import GenesisButton from '@/core/button/GenesisButton.vue'
import CheveronLeftIcon from '../icons/CheveronLeftIcon.vue'
import CheveronRightIcon from '../icons/CheveronRightIcon.vue'

export default defineComponent({
  name: 'PaginationLayout',
  components: { GenesisButton, CheveronLeftIcon, CheveronRightIcon },
  props: {
    start: { required: true, type: Number },
    end: { required: true, type: Number },
    page: { required: true, type: Number },
    quickForm: { required: false, type: Boolean },
  },
  emits: ['update:page'],
  setup(props, { emit }) {
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

    const onChangePage = (page: number | string) => {
      if (!page) {
        page = 1
      }
      if (typeof page == 'string') {
        page = Number.parseInt(page)
      }
      emit('update:page', page)
    }

    const goToSelectedPage = () => {
      onChangePage(selectedPage.value)
    }

    const goToPreviousPage = () => {
      if (props.page > 1) {
        emit('update:page', props.page - 1)
      }
    }

    const goToNextPage = () => {
      if (props.page < props.end) {
        emit('update:page', props.page + 1)
      }
    }

    return {
      pageNumbers,
      onChangePage,
      goToPreviousPage,
      goToNextPage,
      selectedPage,
      pageSelectOptions,
      goToSelectedPage,
    }
  },
})
</script>

<style scoped></style>
