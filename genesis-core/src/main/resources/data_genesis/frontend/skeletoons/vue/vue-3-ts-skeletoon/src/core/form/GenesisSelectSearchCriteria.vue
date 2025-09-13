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
    <div class="relative dropdown w-full min-w-50">
      <input
        @click="dropdownSwitcher"
        class="w-full select cursor-default"
        :aria-expanded="showDropdownState"
        v-model="selectedValue"
        readonly
        :aria-controls="'dropdown-' + inputId"
        :placeholder="placeholder ?? '-- Select an option'"
      />

      <!-- Dropdown (Card style) -->
      <!--        v-show="showDropdownState"-->
      <div
        ref="dropdownRef"
        :id="'dropdown-' + inputId"
        v-show="showDropdownState"
        :class="['absolute mt-1 w-full z-10 top-full', 'card border-1 bg-base-100 shadow-lg']"
        tabindex="-1"
      >
        <!-- Card header -->
        <div class="card-header border-b p-3 flex flex-col items-center gap-2 bg-base-200">
          <div class="w-full flex items-center justify-between">
            <span class="font-semibold">Select an option</span>
            <GenesisButton
              @click="hideDropdown"
              class="btn btn-ghost btn-sm text-error"
              title="Cancel"
              type="button"
            >
              <XIcon />
            </GenesisButton>
          </div>
          <div class="w-full">
            <GenesisSearch
              :initial-model="defaultFilterValue"
              :default-active="defaultFilter"
              :search-fields="filters"
              @search="onSearchInput"
              :auto="true"
            />
          </div>
        </div>

        <!-- Card body -->
        <div ref="listRef" @scroll="onScroll" class="dropdown-content card-body p-0 max-h-72 overflow-y-auto">
          <div v-if="options.length === 0" class="px-3 py-2 text-neutral">No result</div>
          <div
            v-for="option in options"
            :key="option.value"
            @mousedown.left="() => {
              selectOption(option)
              handleClick()
            }"
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
import { ref, computed, onMounted } from 'vue'
import { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter } from '@/models/api/RequestModel'
import type { SelectOption } from '@/models/SelectOption'
import GenesisSearch from '@/core/search/GenesisSearch.vue'
import type { EntitySearchField } from '@/models/EntityModel'
import XIcon from '@/core/icons/XIcon.vue'
import GenesisButton from '@/core/button/GenesisButton.vue'

/* Props */
const props = defineProps<{
  label?: string
  placeholder?: string
  filters: EntitySearchField[]
  searchFunction: (
    filters: Record<string, unknown>,
    pagination: PaginationRequestParameter,
  ) => Promise<{ options: SelectOption[]; pagination: PaginationData }>
  defaultValue?: string
  defaultKey?: string
  loading?: boolean
  rowInput?: boolean
  pageSize?: number
}>()

/* Emits */
const emit = defineEmits<{
  (e: 'option-selected', value: string): void
}>()

/* State */
const showDropdownState = ref(false)
const dropdownRef = ref<HTMLDivElement | null>(null)
const listRef = ref<HTMLDivElement | null>(null)

const selectedValue = ref(props.defaultValue ?? '')
const loadingPage = ref(false)
const hasMore = ref(true)
const currentFilters = ref<Record<string, unknown>>({})
const pagination = ref(
  new PaginationData({
    size: props.pageSize ?? 100,
    number: 0,
  }),
)
const options = ref<SelectOption[]>([])

/* Computed */
const defaultFilter = computed(() => [props.defaultKey ?? ''])
const defaultFilterValue = computed(() =>
  props.defaultKey && props.defaultKey.length > 0 ? { [props.defaultKey]: props.defaultValue } : {},
)

const inputId = computed(() =>
  props.label
    ? 'select-search-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'select-search-' + Math.random().toString(36).substring(2, 8),
)

/* Methods */
const handleClick = () => {
  const elem = document.activeElement as HTMLElement | null;
  if (elem) {
    elem.blur();
  }
};
const loadOptions = async (reset = false) => {
  if (loadingPage.value || !hasMore.value) return

  loadingPage.value = true
  if (reset) {
    pagination.value.reset(props.pageSize ?? 100)
  } else {
    pagination.value = pagination.value.nextPage()
  }

  const result = await props.searchFunction(currentFilters.value, pagination.value.toParameter())

  if (reset) {
    options.value = result.options
  } else {
    options.value = options.value.concat(result.options)
  }

  pagination.value = result.pagination
  hasMore.value = pagination.value.hasNext()
  loadingPage.value = false
}

const updateFilter = (filters: Record<string, unknown>) => {
  currentFilters.value = filters
}

const onSearchInput = async (filters: Record<string, unknown>) => {
  hasMore.value = true
  updateFilter(filters)
  await loadOptions(true)
}

const onScroll = async () => {
  const el = listRef.value
  if (!el) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 1) {
    await loadOptions(false)
  }
}

const selectOption = (option: SelectOption) => {
  selectedValue.value = option.label
  emit('option-selected', option.value)
  hideDropdown()
}

const showDropdown = async () => {
  showDropdownState.value = true
  await loadOptions(true)
}

const dropdownSwitcher = () => {
  showDropdownState.value ? hideDropdown() : showDropdown()
}

const hideDropdown = () => {
  showDropdownState.value = false
}

/* Initialize default option label */
onMounted(async () => {
  if (props.defaultValue && props.defaultKey) {
    const result = await props.searchFunction(
      { [props.defaultKey]: props.defaultValue },
      new PaginationData({ size: 1, number: 0 }).toParameter(),
    )
    if (result.options.length > 0) {
      selectedValue.value = result.options[0].label
    }
  }
})
</script>
