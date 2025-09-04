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
    <div class="relative w-full">
      <input
        v-bind="$attrs"
        :id="inputId"
        v-model="searchModel"
        type="text"
        class="select pr-8 w-full"
        :placeholder="placeholder ?? 'Search...'"
        @input="onSearchInput"
        :disabled="loading"
        @focusin="showDropdown"
        @focusout="hideDropdown"
        autocomplete="off"
      />

      <!-- Dropdown Options -->
      <div
        ref="dropdownRef"
        @scroll="onScroll"
        :class="[
          'mt-1 max-h-72',
          'w-full overflow-y-auto',
          'bg-base-100',
          'absolute',
          'top-11/12',
          'border',
          'z-10',
          { invisible: !showDropdownState },
        ]"
      >
        <div v-if="options.length == 0" class="px-3 py-2 text-neutral">No result</div>
        <div
          v-for="option in options"
          :key="option.value"
          @mousedown="selectOption(option)"
          class="cursor-pointer px-3 py-2 hover:bg-primary hover:text-primary-content"
        >
          {{ option.label }}
        </div>
        <div v-if="loadingPage">
          <span class="loading-spinner"></span>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter } from '@/models/api/RequestModel'
import { type SelectOption } from '@/models/SelectOption'
import { defineComponent, ref, computed, type PropType } from 'vue'

export default defineComponent({
  name: 'GenesisSelectSearch',
  props: {
    label: { type: String, required: false },
    placeholder: { type: String, required: false },
    searchFunction: {
      type: Function as PropType<
        (
          searchTerm: string,
          pagination: PaginationRequestParameter,
        ) => Promise<{ options: SelectOption[]; pagination: PaginationData }>
      >,
      required: true,
    },
    defaultValue: { type: String, default: '' },
    loading: { type: Boolean, default: false },
    rowInput: { type: Boolean, default: false },
    pageSize: { type: Number, default: 10 },
  },
  emits: ['option-selected'],
  setup(props, { emit }) {
    const showDropdownState = ref(false)
    const dropdownRef = ref<HTMLDivElement | null>(null)
    const searchModel = ref(props.defaultValue)
    const loadingPage = ref(false)
    const hasMore = ref(true)
    const pagination = ref<PaginationData>(
      new PaginationData({
        size: props.pageSize,
        number: 0,
      }),
    )
    const options = ref<SelectOption[]>([])

    const loadOptions = async (reset = false) => {
      if (loadingPage.value || !hasMore.value) return

      loadingPage.value = true
      if (reset) {
        pagination.value.reset(props.pageSize)
      } else {
        pagination.value = pagination.value.nextPage()
      }

      const result = await props.searchFunction(searchModel.value, pagination.value.toParameter())
      if (reset) {
        options.value = result.options
      } else {
        options.value = options.value.concat(result.options)
      }
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
      emit('option-selected', option.value)
      hideDropdown()
    }

    const showDropdown = async () => {
      showDropdownState.value = true
      await loadOptions(true)
    }

    const hideDropdown = () => (showDropdownState.value = false)

    const inputId = computed(() =>
      props.label
        ? 'select-search-' + props.label.replace(/\s+/g, '-').toLowerCase()
        : 'select-search-' + Math.random().toString(36).substring(2, 8),
    )

    return {
      showDropdownState,
      dropdownRef,
      searchModel,
      options,
      selectOption,
      showDropdown,
      hideDropdown,
      inputId,
      onSearchInput,
      onScroll,
      loadingPage,
    }
  },
})
</script>
