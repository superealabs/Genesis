<template>
  <div class="flex items-center gap-2" :class="{ 'flex-col items-start': !rowInput }">
    <div class="flex gap-2 bg-transparent">
      <label
        v-if="label || $slots.label"
        :for="inputId"
        class="label font-medium"
        :class="{ 'whitespace-nowrap': rowInput }"
      >
        <slot name="label">{{ label }}</slot>
      </label>
    </div>
    <div class="relative w-full min-w-50 flex-grow">
      <slot
        name="selected-value"
        :value="selectedValue"
        :toggle="toggleDropdownAndLoad"
        :placeholder="placeholder"
      >
        <input
          ref="inputRef"
          @click="toggleDropdownAndLoad"
          class="w-full select cursor-default"
          :class="{ 'border-error': violation }"
          :aria-expanded="showDropdownState"
          v-model="selectedValue"
          readonly
          :aria-controls="'dropdown-' + inputId"
          :placeholder="placeholder ?? '-- Select an option'"
        />
      </slot>

      <div
        ref="dropdownRef"
        :id="'dropdown-' + inputId"
        v-show="showDropdownState"
        class="absolute mt-1 w-full z-10 top-full card border border-base-300 bg-base-200 shadow-lg"
      >
        <div class="card-header p-3 flex flex-col gap-2 bg-base-200">
          <div class="flex justify-between w-full items-center border-b">
            <slot name="header-title">
              <span class="font-semibold">Select an option</span>
            </slot>
            <GenesisButton
              @click="hideDropdown"
              class="btn btn-ghost btn-sm text-error"
              type="button"
              title="Cancel"
            >
              <XIcon />
            </GenesisButton>
          </div>

          <slot name="search-criteria" :filters="filters" :on-search="onSearchInput">
            <div @mousedown.stop @click.stop>
              <GenesisSearch
                :initial-model="defaultFilterValue"
                :default-active="defaultFilter"
                :search-fields="filters"
                @search="onSearchInput"
                :auto="true"
              />
            </div>
          </slot>
        </div>

        <div
          ref="listRef"
          @scroll="onScroll"
          class="card-body border-t p-0 max-h-72 overflow-y-auto"
        >
          <slot
            name="options-list"
            :options="options"
            :loading-page="loadingPage"
            :select-option="selectOption"
          >
            <div v-if="options.length === 0" class="px-3 py-6 text-center">No result</div>

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
          </slot>
        </div>
      </div>

      <ErrorMessage v-if="violation" :message="violation" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
// 1. Import useDropdown
import { useDropdown } from '@/composables/useDropdown'
import { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter } from '@/models/api/RequestModel'
import type { SelectOption } from '@/models/SelectOption'
import GenesisSearch from '@/components/search/GenesisSearch.vue'
import type { EntitySearchField } from '@/models/EntityModel'
import XIcon from '@/components/icons/XIcon.vue'
import GenesisButton from '@/components/button/GenesisButton.vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

defineOptions({
  name: 'GenesisSelectSearchCriteria',
  inheritAttrs: false,
})

/* Props & Emits (Unchanged) */
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
  violation?: string
}>()

const emit = defineEmits<{
  (e: 'option-selected', value: string | number): void
}>()

/* State */
// 2. Use refs for trigger and content, but no longer need manual setup/teardown in onMounted/onBeforeUnmount
const inputRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLDivElement | null>(null)
const listRef = ref<HTMLDivElement | null>(null)

// 3. Destructure properties from the composable, replacing manual state and logic
const {
  showDropdown: showDropdownState, // Use an alias to match the original state variable name
  openDropdown,
  hideDropdown,
} = useDropdown(inputRef, dropdownRef) // `as any` might be needed if types are strict

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

/* Computed (Unchanged) */
const defaultFilter = computed(() => [props.defaultKey ?? ''])
const defaultFilterValue = computed(() =>
  props.defaultKey && props.defaultKey.length > 0 ? { [props.defaultKey]: props.defaultValue } : {},
)

const inputId = computed(() =>
  props.label
    ? 'select-search-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'select-search-' + Math.random().toString(36).substring(2, 8),
)

/* Dropdown control (Modified to incorporate data loading) */
// This custom function is needed because we must load data *before* opening the dropdown.
const openDropdownAndLoad = async () => {
  // Use the openDropdown from the composable to set state
  openDropdown()
  await loadOptions(true)
}

const toggleDropdownAndLoad = () => {
  if (showDropdownState.value == true) {
    hideDropdown()
  } else {
    openDropdownAndLoad()
  }
}

/* Lifecycle Hook (Modified to remove manual click outside logic) */
onMounted(() => {
  // Keep the default value initialization logic
  if (props.defaultValue && props.defaultKey) {
    props
      .searchFunction(
        { [props.defaultKey]: props.defaultValue },
        new PaginationData({ size: 1, number: 0 }).toParameter(),
      )
      .then((result) => {
        if (result.options.length > 0) {
          selectedValue.value = result.options[0].label
        }
      })
  }
})

onBeforeUnmount(() => {
  // Nothing needed here, useDropdown cleans up its event listener
})

/* Search + infinite scroll (Unchanged) */
const loadOptions = async (reset = false) => {
  if (loadingPage.value || !hasMore.value) return

  loadingPage.value = true
  if (reset) {
    pagination.value.reset(props.pageSize ?? 100)
  } else {
    pagination.value = pagination.value.nextPage()
  }

  const result = await props.searchFunction(currentFilters.value, pagination.value.toParameter())
  options.value = reset ? result.options : options.value.concat(result.options)
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

/* Option select (Uses hideDropdown from composable) */
const selectOption = (option: SelectOption) => {
  selectedValue.value = option.label
  emit('option-selected', option.value)
  hideDropdown()
}
</script>

<style scoped>
.select {
  min-width: 120px;
}

/* Scrollbar stable */
.card-body {
  scrollbar-gutter: stable;
}
</style>
