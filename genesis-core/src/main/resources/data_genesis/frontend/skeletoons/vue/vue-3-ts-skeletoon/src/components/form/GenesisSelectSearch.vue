<template>
  <div class="flex items-center gap-2" :class="{ 'flex-col items-start': !rowInput }">
    <!-- Label -->
    <label
      v-if="label"
      :for="inputId"
      class="label font-medium"
      :class="{ 'whitespace-nowrap': rowInput }"
    >
      {{ label }}
    </label>

    <!-- Dropdown wrapper -->
    <div class="relative w-full min-w-50">
      <!-- Trigger Button -->
      <input
        ref="inputRef"
        @click="toggleDropdown"
        class="border-0 w-full select cursor-default"
        :aria-expanded="showDropdown"
        v-model="selectedOption"
        readonly
        :aria-controls="'dropdown-' + inputId"
        :placeholder="placeholder ?? '-- Select an option'"
      />

      <!-- Dropdown -->
      <div
        v-show="showDropdown"
        ref="dropdownRef"
        :id="'dropdown-' + inputId"
        class="absolute mt-1 w-auto z-10 card border border-base-300 bg-base-200 shadow-lg"
        tabindex="-1"
      >
        <!-- Header (search input) -->
        <div class="card-header border-b bg-base-200 p-2">
          <input
            v-model="searchModel"
            type="text"
            class="input input-bordered w-full"
            :placeholder="placeholder ?? 'Search...'"
            @input="onSearchInput"
            :disabled="loading"
            autocomplete="off"
          />
        </div>

        <!-- Body (list of options) -->
        <div ref="listRef" @scroll="onScroll" class="card-body p-0 max-h-64 overflow-y-auto">
          <div
            v-if="options.length === 0 && !loadingPage"
            class="px-3 py-2 text-neutral text-sm text-center"
          >
            No results found
          </div>

          <div
            v-for="option in options"
            :key="option.value"
            @mousedown.prevent="selectOption(option)"
            class="cursor-pointer px-3 py-2 hover:bg-primary hover:text-primary-content"
          >
            {{ option.label }}
          </div>

          <div v-if="loadingPage" class="flex justify-center py-2">
            <span class="loading loading-spinner"></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter } from '@/models/api/RequestModel'
import type { SelectOption } from '@/models/SelectOption'

const props = defineProps<{
  label?: string
  placeholder?: string
  searchFunction: (
    searchTerm: string,
    pagination: PaginationRequestParameter,
  ) => Promise<{ options: SelectOption[]; pagination: PaginationData }>
  defaultValue?: string
  loading?: boolean
  rowInput?: boolean
  pageSize?: number
}>()

const emit = defineEmits<{
  (e: 'option-selected', value: string | number): void
}>()

/* State */
const showDropdown = ref(false)
const dropdownRef = ref<HTMLDivElement | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)
const listRef = ref<HTMLDivElement | null>(null)
const selectedOption = ref(props.defaultValue ?? '')
const searchModel = ref(props.defaultValue ?? '')
const loadingPage = ref(false)
const hasMore = ref(true)
const pagination = ref(new PaginationData({ size: props.pageSize ?? 10, number: 0 }))
const options = ref<SelectOption[]>([])

/* Computed */
const inputId = computed(() =>
  props.label
    ? 'select-search-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'select-search-' + Math.random().toString(36).substring(2, 8),
)

/* Core Methods */
const toggleDropdown = () => {
  if (showDropdown.value == true) {
    hideDropdown()
  } else {
    openDropdown()
  }
}

const openDropdown = async () => {
  showDropdown.value = true
  await loadOptions(true)
}

const hideDropdown = () => {
  showDropdown.value = false
}

/* Click outside handler */
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as Node
  if (dropdownRef.value?.contains(target) || inputRef.value?.contains(target)) return
  hideDropdown()
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

/* Fetch options (with pagination) */
const loadOptions = async (reset = false) => {
  if (loadingPage.value || !hasMore.value) return

  loadingPage.value = true
  if (reset) pagination.value.reset(props.pageSize ?? 10)
  else pagination.value = pagination.value.nextPage()

  const result = await props.searchFunction(searchModel.value, pagination.value.toParameter())
  options.value = reset ? result.options : options.value.concat(result.options)
  pagination.value = result.pagination
  hasMore.value = pagination.value.hasNext()
  loadingPage.value = false
}

/* Search & Infinite Scroll */
const onSearchInput = async () => {
  hasMore.value = true
  await loadOptions(true)
}

const onScroll = async () => {
  const el = listRef.value
  if (!el) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 1) {
    await loadOptions(false)
  }
}

/* Select Option */
const selectOption = (option: SelectOption) => {
  selectedOption.value = option.label
  emit('option-selected', option.value)
  hideDropdown()
}
</script>

<style scoped>
.card-body {
  scrollbar-gutter: stable;
}
</style>
