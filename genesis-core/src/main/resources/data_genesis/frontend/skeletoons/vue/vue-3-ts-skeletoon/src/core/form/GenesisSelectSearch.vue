<template>
  <div class="flex items-center gap-2" :class="{ 'flex-col items-start': !rowInput }">
    <!-- Label -->
    <label
      v-if="label"
      :for="inputId"
      class="label font-medium text-neutral"
      :class="{ 'whitespace-nowrap': rowInput }"
    >
      {{ label }}
    </label>

    <!-- Search Input -->
    <div class="relative w-full min-w-25">
      <!-- Button to toggle dropdown -->
      <button
        @click="dropdownSwithcer"
        type="button"
        class="btn w-full select"
        :aria-expanded="showDropdownState"
        :aria-controls="'dropdown-' + inputId"
      >
        <!-- Show selected value or placeholder -->
        <span>
          {{ selectedOption ?? '-- Select an option' }}
        </span>
      </button>

      <!-- Dropdown (Card style) -->
      <div
        ref="dropdownRef"
        :id="'dropdown-' + inputId"
        v-show="showDropdownState"
        :class="['absolute mt-1 w-full z-10 top-full', 'card border-1 bg-base-100 shadow-lg']"
        tabindex="-1"
      >
        <!-- Card header: search input -->
        <div class="card-header p-3 border-b flex items-center gap-2 bg-base-200">
          <input
            v-bind="$attrs"
            :id="inputId"
            v-model="searchModel"
            type="text"
            class="input input-bordered w-full"
            :placeholder="placeholder ?? 'Search...'"
            @input="onSearchInput"
            :disabled="loading"
            autocomplete="off"
          />
        </div>
        <!-- Card body: options list -->
        <div @scroll="onScroll" class="card-body p-0 max-h-72 overflow-y-auto">
          <div v-if="options.length === 0" class="px-3 py-2 text-neutral">No result</div>
          <div
            v-for="option in options"
            :key="option.value"
            @mousedown.left="selectOption(option)"
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
import { ref, computed } from 'vue'
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
  (e: 'option-selected', value: string): void
}>()

// ---------------- State ----------------
const showDropdownState = ref(false)
const dropdownRef = ref<HTMLDivElement | null>(null)
const selectedOption = ref(props.defaultValue ?? '')
const searchModel = ref(props.defaultValue ?? '')
const loadingPage = ref(false)
const hasMore = ref(true)
const pagination = ref<PaginationData>(
  new PaginationData({
    size: props.pageSize ?? 10,
    number: 0,
  }),
)
const options = ref<SelectOption[]>([])

// ---------------- Logic ----------------
const loadOptions = async (reset = false) => {
  if (loadingPage.value || !hasMore.value) return

  loadingPage.value = true
  if (reset) {
    pagination.value.reset(props.pageSize ?? 10)
  } else {
    pagination.value = pagination.value.nextPage()
  }

  const result = await props.searchFunction(searchModel.value, pagination.value.toParameter())

  options.value = reset ? result.options : options.value.concat(result.options)
  pagination.value = result.pagination
  hasMore.value = pagination.value.hasNext()
  loadingPage.value = false
}

const onSearchInput = async () => {
  hasMore.value = true
  await loadOptions(true)
}

const onScroll = async () => {
  const el = dropdownRef.value
  if (!el) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 1) {
    await loadOptions(false)
  }
}

const selectOption = (option: SelectOption) => {
  searchModel.value = option.label
  selectedOption.value = option.label
  emit('option-selected', option.value)
  hideDropdown()
}

const showDropdown = async () => {
  showDropdownState.value = true
  await loadOptions(true)
}

const dropdownSwithcer = () => {
  showDropdownState.value ? hideDropdown() : showDropdown()
}

const hideDropdown = () => {
  showDropdownState.value = false
}

const inputId = computed(() =>
  props.label
    ? 'select-search-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'select-search-' + Math.random().toString(36).substring(2, 8),
)

defineOptions({
  name: 'GenesisSelectSearch',
})
</script>
